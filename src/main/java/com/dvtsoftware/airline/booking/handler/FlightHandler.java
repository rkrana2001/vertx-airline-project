package com.dvtsoftware.airline.booking.handler;

import com.dvtsoftware.airline.booking.model.Flight;
import com.dvtsoftware.airline.booking.service.DatabaseService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.sqlclient.Tuple;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FlightHandler {
  private final DatabaseService dbService;

  public FlightHandler(DatabaseService dbService) {
    this.dbService = dbService;
  }

    public void addFlight(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        if (body == null || !body.containsKey("airlineId") || !body.containsKey("flightNumber")) {
            rc.fail(new HttpException(400, "Missing required flight fields."));
            return;
        }

        // SQL updated to match your CREATE TABLE schema
        String sql = "INSERT INTO flights (" +
                "flight_number, airline_id, departure_airport, arrival_airport, " +
                "departure_time, arrival_time, available_seats, total_seats, price" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime departure = LocalDateTime.parse(body.getString("departureTime"));
        LocalDateTime arrival = LocalDateTime.parse(body.getString("arrivalTime"));

        Tuple params = Tuple.of(
                body.getString("flightNumber"),
                body.getLong("airlineId"),
                body.getString("origin"),
                body.getString("destination"),
                departure, // Pass as object
                arrival,   // Pass as object
                body.getInteger("seatsAvailable", 0),
                body.getInteger("totalSeats", 100),
                body.getDouble("price", 0.0)
        );

        dbService.getPool().preparedQuery(sql).execute(params)
                .onSuccess(rows -> {
                    Long generatedId = rows.property(io.vertx.jdbcclient.JDBCPool.GENERATED_KEYS).getLong(0);
                    fetchAndSendFlight(Math.toIntExact(generatedId), rc);
                })
                .onFailure(err -> {
                    err.printStackTrace();
                    rc.fail(new HttpException(500, "Database error: " + err.getMessage()));
                });
    }

  public void getFlight(RoutingContext rc) {
    int id = Integer.parseInt(rc.pathParam("id"));
    dbService.getPool().preparedQuery("SELECT * FROM Flights WHERE id = ?").execute(Tuple.of(id))
            .map(rows -> rows.iterator().hasNext() ? Flight.fromRow(rows.iterator().next()) : null)
            .onSuccess(f -> {
              if (f == null) rc.fail(new HttpException(HttpURLConnection.HTTP_NOT_FOUND, "Flight not found."));
              else rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(f));
            })
            .onFailure(err -> rc.fail(new HttpException(HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to fetch flight.")));
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

    /*public void searchFlights(RoutingContext rc) {
        // 1. Match the keys used in your URL (?origin=...&destination=...)
        String from = rc.queryParam("origin").stream().findFirst().orElse(null);
        String to = rc.queryParam("destination").stream().findFirst().orElse(null);

        if (from == null || to == null) {
            rc.fail(400, new Exception("origin and destination query parameters are required."));
            return;
        }

        // 2. Use the ACTUAL column names from your CREATE TABLE script
        String sql = "SELECT * FROM flights WHERE departure_airport = ? AND arrival_airport = ?";

        dbService.getPool().preparedQuery(sql)
                .execute(Tuple.of(from, to))
                .map(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(Flight::fromRow)
                        .collect(Collectors.toList()))
                .onSuccess(list -> rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    err.printStackTrace();
                    rc.fail(500, err);
                });
    }*/

    public void searchFlights(RoutingContext rc) {
        // 1. Extract all query parameters
        String from = rc.queryParam("origin").stream().findFirst().orElse(null);
        String to = rc.queryParam("destination").stream().findFirst().orElse(null);
        String depDate = rc.queryParam("departure").stream().findFirst().orElse(null);
        String arrDate = rc.queryParam("arrival").stream().findFirst().orElse(null);

        // Origin and Destination remain mandatory
        if (from == null || to == null) {
            rc.fail(400, new Exception("origin and destination query parameters are required."));
            return;
        }
        if (depDate != null) {
            try {
                LocalDate.parse(depDate); // This checks if the date is valid (e.g., rejects Feb 31)
            } catch (DateTimeParseException e) {
                rc.fail(400, new Exception("Invalid departure date: " + depDate ));
                return;
            }
        }

        if (arrDate != null) {
            try {
                LocalDate.parse(arrDate);
            } catch (DateTimeParseException e) {
                rc.fail(400, new Exception("Invalid arrival date: " + arrDate));
                return;
            }
        }

        // 2. Build Dynamic SQL
        // We use CAST(column AS DATE) to ignore the time part during comparison
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

        dbService.getPool().preparedQuery(sql.toString())
                .execute(params)
                .map(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(Flight::fromRow)
                        .collect(Collectors.toList()))
                .onSuccess(list -> rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    err.printStackTrace();
                    rc.fail(500, err);
                });
    }
}