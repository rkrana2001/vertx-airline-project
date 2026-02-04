package com.airline.booking.handler;

import com.airline.booking.model.Flight;
import com.airline.booking.service.DatabaseService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightHandler {
    private static final Logger log = LoggerFactory.getLogger(com.airline.booking.handler.FlightHandler.class);
    private final DatabaseService dbService;

    public FlightHandler(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void addFlight(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        if (body == null || !body.containsKey("airlineId") || !body.containsKey("flightNumber")) {
            rc.fail(new HttpException(400, "Missing required flight fields: airlineId, flightNumber"));
            return;
        }

        // Tightened Date Parsing
        LocalDateTime departure;
        LocalDateTime arrival;
        try {
            departure = LocalDateTime.parse(body.getString("departureTime"));
            arrival = LocalDateTime.parse(body.getString("arrivalTime"));
        } catch (DateTimeParseException | NullPointerException e) {
            rc.fail(new HttpException(400, "Invalid date format. Use ISO-8601 (e.g., 2025-12-25T10:30:00)"));
            return;
        }

        String sql = "INSERT INTO flights (flight_number, airline_id, departure_airport, arrival_airport, " +
                "departure_time, arrival_time, available_seats, total_seats, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Tuple params = Tuple.of(
                body.getString("flightNumber"),
                body.getLong("airlineId"),
                body.getString("from"),
                body.getString("to"),
                departure,
                arrival,
                body.getInteger("seatsAvailable", 0),
                body.getInteger("totalSeats", 100),
                body.getDouble("price", 0.0)
        );

        dbService.getPool().preparedQuery(sql).execute(params)
                .onSuccess(rows -> {
                    Long generatedId = rows.property(io.vertx.jdbcclient.JDBCPool.GENERATED_KEYS).getLong(0);
                    log.info("Flight successfully inserted into database. Generated ID: {}", generatedId);
                    fetchAndSendFlight(Math.toIntExact(generatedId), rc);
                })
                .onFailure(err -> {
                    log.error("Flight insertion failed", err);
                    log.error("Flight insertion failed for flight {}: {}", body.getString("flightNumber"), err.getMessage());
                    rc.fail(new HttpException(409, "Could not create flight. Check if airline exists or flight number is duplicate."));
                });
    }

    public void getFlight(RoutingContext rc) {
        // Tightened ID Parsing
        int id;
        try {
            id = Integer.parseInt(rc.pathParam("id"));
        } catch (NumberFormatException e) {
            rc.fail(new HttpException(400, "Invalid flight ID format."));
            return;
        }

        dbService.getPool().preparedQuery("SELECT * FROM Flights WHERE id = ?").execute(Tuple.of(id))
                .map(rows -> rows.iterator().hasNext() ? Flight.fromRow(rows.iterator().next()) : null)
                .onSuccess(f -> {
                    if (f == null) {
                        log.warn("Flight fetch failed: ID {} not found", id);
                        rc.fail(new HttpException(404, "Flight not found."));
                    }else {
                        log.info("Successfully retrieved flight details for ID: {}", id);
                        rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(f));
                    }})
                .onFailure(err -> {
                    log.error("Fetch flight failed", err);
                    log.error("Fetch flight failed for ID {}: {}", id, err.getMessage());
                    rc.fail(new HttpException(500, "Internal server error while fetching flight."));
                });
    }

    private void fetchAndSendFlight(Integer id, RoutingContext rc) {
        dbService.getPool().preparedQuery("SELECT * FROM Flights WHERE id = ?")
                .execute(Tuple.of(id))
                .onSuccess(rows -> {
                    if (rows.iterator().hasNext()) {
                        rc.response()
                                .setStatusCode(201)
                                .putHeader("Content-Type", "application/json")
                                .end(Json.encodePrettily(Flight.fromRow(rows.iterator().next())));
                    } else {
                        rc.fail(404);
                    }
                })
                .onFailure(rc::fail);
    }


    public void searchFlights(RoutingContext rc) {
        String from = rc.queryParam("from").stream().findFirst().orElse(null);
        String to = rc.queryParam("to").stream().findFirst().orElse(null);
        String depDate = rc.queryParam("departure").stream().findFirst().orElse(null);
        String arrDate = rc.queryParam("arrival").stream().findFirst().orElse(null);

        if (from == null || to == null) {
            rc.fail(new HttpException(400, "Origin ('from') and destination ('to') query parameters are required."));
            return;
        }

        // Validate dates without leaking internal stack traces
        try {
            if (depDate != null) LocalDate.parse(depDate);
            if (arrDate != null) LocalDate.parse(arrDate);
        } catch (DateTimeParseException e) {
            rc.fail(new HttpException(400, "Invalid date format. Expected YYYY-MM-DD."));
            return;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM flights WHERE departure_airport = ? AND arrival_airport = ?");
        Tuple params = Tuple.of(from, to);

        if (depDate != null) {
            sql.append(" AND CAST(departure_time AS DATE) = ?");
            params.addString(depDate);
        }
        if (arrDate != null) {
            sql.append(" AND CAST(arrival_time AS DATE) = ?");
            params.addString(arrDate);
        }

        dbService.getPool().preparedQuery(sql.toString()).execute(params)
                .map(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(Flight::fromRow)
                        .collect(Collectors.toList()))
                .onSuccess(list -> rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    log.error("Search flights failed", err);
                    rc.fail(new HttpException(500, "An error occurred while searching for flights."));
                });
    }
}