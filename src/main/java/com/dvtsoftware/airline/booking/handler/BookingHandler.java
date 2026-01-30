package com.dvtsoftware.airline.booking.handler;

import com.dvtsoftware.airline.booking.model.Booking;
import com.dvtsoftware.airline.booking.service.DatabaseService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BookingHandler {
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

                conn.preparedQuery(
                                "SELECT 1 FROM bookings WHERE passenger_id = ? AND flight_id = ?")
                        .execute(Tuple.of(passengerId, flightId))
                        .compose(rows -> {
                            if (rows.iterator().hasNext()) {
                                return Future.failedFuture(
                                        new HttpException(409, "Passenger already booked this flight"));
                            }
                            return Future.succeededFuture();
                        })

                        .compose(v ->
                                conn.preparedQuery(
                                                "SELECT available_seats, price FROM flights WHERE id = ?")
                                        .execute(Tuple.of(flightId))
                        )
                        .compose(rows -> {
                            if (!rows.iterator().hasNext()) {
                                return Future.failedFuture(new HttpException(404, "Flight not found"));
                            }

                            Row flightRow = rows.iterator().next();
                            int available = flightRow.getInteger("AVAILABLE_SEATS");
                            double price = flightRow.getDouble("PRICE");

                            if (available <= 0) {
                                return Future.failedFuture(new HttpException(409, "Flight is full"));
                            }

                            return conn.preparedQuery(
                                            "SELECT 1 FROM bookings WHERE flight_id = ? AND seat_number = ?")
                                    .execute(Tuple.of(flightId, seatNumber))
                                    .compose(seatRows -> {
                                        if (seatRows.iterator().hasNext()) {
                                            return Future.failedFuture(
                                                    new HttpException(409, "Seat already booked"));
                                        }
                                        return Future.succeededFuture(price);
                                    });
                        })

                        .compose(price ->
                                conn.preparedQuery(
                                                "UPDATE flights SET available_seats = available_seats - 1 WHERE id = ?")
                                        .execute(Tuple.of(flightId))
                                        .map(price)
                        )

                        .compose(price -> {
                            String sql =
                                    "INSERT INTO bookings " +
                                            "(booking_reference, passenger_id, flight_id, seat_number, status, total_amount) " +
                                            "VALUES (?, ?, ?, ?, 'CONFIRMED', ?)";

                            return conn.preparedQuery(sql)
                                    .execute(Tuple.of(
                                            bookingRef, passengerId, flightId, seatNumber, price));
                        })

                        .compose(result -> {
                            Long generatedId =
                                    result.property(JDBCPool.GENERATED_KEYS).getLong(0);

                            return conn.preparedQuery(
                                            "SELECT * FROM bookings WHERE id = ?")
                                    .execute(Tuple.of(generatedId));
                        })

                        .map(rows ->
                                rows.iterator().hasNext()
                                        ? Booking.fromRow(rows.iterator().next())
                                        : null
                        )

        ).onSuccess(booking ->
                rc.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(booking))
        ).onFailure(rc::fail
        );
    }


    public void retrieveBookingDetails(RoutingContext rc) {
        int id = Integer.parseInt(rc.pathParam("id"));
        dbService.getPool().preparedQuery("SELECT * FROM Bookings WHERE id = ?").execute(Tuple.of(id))
                .map(rows -> rows.iterator().hasNext() ? Booking.fromRow(rows.iterator().next()) : null)
                .onSuccess(b -> {
                    if (b == null) rc.fail(new HttpException(HttpURLConnection.HTTP_NOT_FOUND, "Booking not found."));
                    else rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(b));
                }).onFailure(
                        err -> rc.fail(new HttpException(HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to fetch booking.")));
    }

    public void cancelBooking(RoutingContext rc) {
        // 1. Get the ID from the path parameter
        String idParam = rc.pathParam("id");
        long bookingId = Long.parseLong(idParam);

        dbService.getPool().withTransaction(conn ->
                        // 2. Find the booking and get the flight_id (Note: Upper case column names for H2)
                        conn.preparedQuery("SELECT flight_id FROM bookings WHERE id = ? AND status = 'CONFIRMED'")
                                .execute(Tuple.of(bookingId)).compose(rows -> {
                                    if (!rows.iterator().hasNext()) {
                                        // This triggers the 404 Not Found
                                        return Future.failedFuture(new HttpException(404, "Booking not found or already cancelled."));
                                    }
                                    // H2 usually returns column names as FLIGHT_ID
                                    return Future.succeededFuture(rows.iterator().next().getLong("FLIGHT_ID"));
                                }).compose(flightId ->
                                        // 3. Mark the booking as CANCELLED
                                        conn.preparedQuery("UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")
                                                .execute(Tuple.of(bookingId)).map(v -> flightId) // Pass flightId to the next step
                                ).compose(flightId ->
                                        // 4. Return the seat to the flight inventory
                                        conn.preparedQuery("UPDATE flights SET available_seats = available_seats + 1 WHERE id = ?")
                                                .execute(Tuple.of(flightId))))
                .onSuccess(v -> rc.response().setStatusCode(204).end()) // No Content
                .onFailure(err -> {
                    err.printStackTrace(); // This helps you see the REAL cause in the console
                    int status = (err instanceof HttpException h) ? h.getStatusCode() : 500;
                    rc.fail(status, err);
                });
    }

    public void listPassengerBookings(RoutingContext rc) {
        int pid = Integer.parseInt(rc.pathParam("id"));
        dbService.getPool().preparedQuery("SELECT * FROM Bookings WHERE passenger_id = ?").execute(Tuple.of(pid))
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Booking::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
                .onFailure(err -> rc.fail(
                        new HttpException(HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to list bookings.")));
    }
}
