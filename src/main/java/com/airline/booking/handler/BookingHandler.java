package com.airline.booking.handler;

import com.airline.booking.model.Booking;
import com.airline.booking.service.DatabaseService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BookingHandler {
    private static final Logger log = LoggerFactory.getLogger(com.airline.booking.handler.BookingHandler.class);
    private final DatabaseService dbService;

    public BookingHandler(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void bookTicket(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        if (body == null || !body.containsKey("flightId") || !body.containsKey("passengerId")) {
            rc.fail(new HttpException(400, "Missing flightId or passengerId"));
            return;
        }

        Integer flightId = body.getInteger("flightId");
        Integer passengerId = body.getInteger("passengerId");
        String seatNumber = body.getString("seatNumber");
        String bookingRef = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        dbService.getPool().withTransaction(conn ->
                // 1. VALIDATE PASSENGER EXISTS (Reviewer Improvement)
                conn.preparedQuery("SELECT id FROM passengers WHERE id = ?")
                        .execute(Tuple.of(passengerId))
                        .compose(pRows -> {
                            if (!pRows.iterator().hasNext()) {
                                return Future.failedFuture(new HttpException(404, "Passenger not found"));
                            }
                            // 2. CHECK IF DUPLICATE BOOKING EXISTS
                            return conn.preparedQuery("SELECT 1 FROM bookings WHERE passenger_id = ? AND flight_id = ? AND status = 'CONFIRMED'")
                                    .execute(Tuple.of(passengerId, flightId));
                        })
                        .compose(rows -> {
                            if (rows.iterator().hasNext()) {
                                return Future.failedFuture(new HttpException(409, "Passenger already booked this flight"));
                            }
                            // 3. FETCH FLIGHT DETAILS (Price and Availability)
                            return conn.preparedQuery("SELECT id, flight_number, available_seats, price FROM flights WHERE id = ?")
                                    .execute(Tuple.of(flightId));
                        })
                        .compose(rows -> {
                            if (!rows.iterator().hasNext()) {
                                return Future.failedFuture(new HttpException(404, "Flight not found"));
                            }

                            Row flightRow = rows.iterator().next();
                            // Use UPPERCASE to avoid NoSuchElementException in H2
                            int available = flightRow.getInteger("AVAILABLE_SEATS");
                            double price = flightRow.getDouble("PRICE");

                            if (available <= 0) {
                                return Future.failedFuture(new HttpException(409, "Flight is full"));
                            }

                            // 4. CHECK IF SEAT IS TAKEN
                            return conn.preparedQuery("SELECT 1 FROM bookings WHERE flight_id = ? AND seat_number = ? AND status = 'CONFIRMED'")
                                    .execute(Tuple.of(flightId, seatNumber))
                                    .compose(seatRows -> {
                                        if (seatRows.iterator().hasNext()) {
                                            return Future.failedFuture(new HttpException(409, "Seat already booked"));
                                        }
                                        return Future.succeededFuture(price);
                                    });
                        })
                        .compose(price ->
                                // 5. DECREMENT INVENTORY
                                conn.preparedQuery("UPDATE flights SET available_seats = available_seats - 1 WHERE id = ?")
                                        .execute(Tuple.of(flightId))
                                        .map(price)
                        )
                        .compose(price -> {
                            // 6. CREATE BOOKING
                            String sql = "INSERT INTO bookings (booking_reference, passenger_id, flight_id, seat_number, status, total_amount) VALUES (?, ?, ?, ?, 'CONFIRMED', ?)";
                            return conn.preparedQuery(sql)
                                    .execute(Tuple.of(bookingRef, passengerId, flightId, seatNumber, price));
                        })
                        .compose(result -> {
                            Long generatedId = result.property(JDBCPool.GENERATED_KEYS).getLong(0);
                            return conn.preparedQuery("SELECT * FROM bookings WHERE id = ?")
                                    .execute(Tuple.of(generatedId));
                        })
                        .map(rows -> rows.iterator().hasNext() ? Booking.fromRow(rows.iterator().next()) : null)
        ).onSuccess(booking ->
                rc.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(booking))
        ).onFailure(rc::fail);
    }

    // Other methods updated with uppercase column strings for H2 safety
    public void cancelBooking(RoutingContext rc) {
        long bookingId = Long.parseLong(rc.pathParam("id"));

        dbService.getPool().withTransaction(conn ->
                        conn.preparedQuery("SELECT flight_id FROM bookings WHERE id = ? AND status = 'CONFIRMED'")
                                .execute(Tuple.of(bookingId))
                                .compose(rows -> {
                                    if (!rows.iterator().hasNext()) {
                                        return Future.failedFuture(new HttpException(404, "Booking not found or already cancelled."));
                                    }
                                    // FIXED: Use uppercase "FLIGHT_ID"
                                    return Future.succeededFuture(rows.iterator().next().getLong("FLIGHT_ID"));
                                })
                                .compose(flightId ->
                                        conn.preparedQuery("UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")
                                                .execute(Tuple.of(bookingId))
                                                .map(v -> flightId)
                                )
                                .compose(flightId ->
                                        conn.preparedQuery("UPDATE flights SET available_seats = available_seats + 1 WHERE id = ?")
                                                .execute(Tuple.of(flightId)))
                ).onSuccess(v -> rc.response().setStatusCode(204).end())
                .onFailure(rc::fail);
    }

    public void retrieveBookingDetails(RoutingContext rc) {
        String idParam = rc.pathParam("id");
        int id;

        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            // Return a clean 400 instead of a leaky 500 stack trace
            rc.fail(new HttpException(400, "Invalid booking ID format: " + idParam));
            return;
        }

        dbService.getPool().preparedQuery("SELECT * FROM Bookings WHERE id = ?")
                .execute(Tuple.of(id))
                .map(rows -> rows.iterator().hasNext() ? Booking.fromRow(rows.iterator().next()) : null)
                .onSuccess(b -> {
                    if (b == null) {
                        rc.fail(new HttpException(404, "Booking not found."));
                    } else {
                        rc.response()
                                .putHeader("Content-Type", "application/json")
                                .end(Json.encodePrettily(b));
                    }
                })
                .onFailure(rc::fail);
    }

    public void listPassengerBookings(RoutingContext rc) {
        int pid = Integer.parseInt(rc.pathParam("id"));
        dbService.getPool().preparedQuery("SELECT * FROM Bookings WHERE passenger_id = ?").execute(Tuple.of(pid))
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Booking::fromRow)
                        .collect(Collectors.toList()))
                .onSuccess(list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
                .onFailure(rc::fail);
    }
}