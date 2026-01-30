package com.dvtsoftware.airline.booking.handler;

import com.dvtsoftware.airline.booking.model.Airline;
import com.dvtsoftware.airline.booking.service.DatabaseService;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class AirlineHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AirlineHandler.class);

    private final DatabaseService dbService;

    public AirlineHandler(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void addAirline(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();

        // 1. Validation
        if (body == null || !body.containsKey("name") || !body.containsKey("code")) {
            rc.fail(new HttpException(400, "Missing fields: name, code"));
            return;
        }

        // 2. Map JSON to Model
        Airline airline = new Airline(null, body.getString("name"), body.getString("code"),
                body.getString("country", "Unknown"));

        // 3. Execute Async Database Query directly
        dbService.getPool().preparedQuery("INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)")
                .execute(Tuple.of(airline.getName(), airline.getCode(), airline.getCountry())).onSuccess(rows -> {
                    // H2/JDBC Client returns the generated keys in the 'rows' object
                    // but we need to extract the ID specifically.
                    Long generatedId = rows.property(JDBCPool.GENERATED_KEYS).getLong(0);

                    airline.setId(generatedId); // Set the ID back into your object

                    rc.response().setStatusCode(201).putHeader("Content-Type", "application/json")
                            .end(Json.encodePrettily(airline));
                }).onFailure(err -> {
                    LOG.error("Registration failed", err);
                    rc.fail(new HttpException(500, "Database error: " + err.getMessage()));
                });
    }


    public void listAllAirlines(RoutingContext rc) {
        // Keep it simple - H2 will treat these as ID, NAME, etc.
        String sql = "SELECT id, name, code, country FROM airlines ORDER BY name";

        dbService.getPool().query(sql).execute()
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Airline::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    LOG.error("Retrieval failed", err);
                    //err.printStackTrace();
                    rc.fail(new HttpException(500, "Failed to retrieve airlines."));
                });
    }

}
