package com.airline.booking.handler;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.airline.booking.model.Airline;
import com.airline.booking.service.DatabaseService;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;

public class AirlineHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AirlineHandler.class);

    private final DatabaseService dbService;

    public AirlineHandler(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void addAirline(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();

        // 1. Validation
        if (body == null || body.getString("name") == null || body.getString("code") == null) {
            rc.fail(new HttpException(400, "Missing fields: name, code"));
            return;
        }

        // 2. Create record (id = null initially)
        Airline airline = new Airline(null, body.getString("name"), body.getString("code"),
                body.getString("country", "Unknown"));

        // 3. Insert into DB
        dbService.getPool().preparedQuery("INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)")
                .execute(Tuple.of(airline.name(), airline.code(), airline.country())).onSuccess(rows -> {
                    Long generatedId = rows.property(JDBCPool.GENERATED_KEYS).getLong(0);

                    // 4. Create NEW Airline with ID
                    Airline saved = new Airline(generatedId, airline.name(), airline.code(), airline.country());

                    rc.response().setStatusCode(201).putHeader("Content-Type", "application/json")
                            .end(Json.encodePrettily(saved));
                }).onFailure(err -> {
                    rc.fail(new HttpException(409, "Failed to create airline"));
                });
    }

    public void listAllAirlines(RoutingContext rc) {
        String sql = "SELECT id, name, code, country FROM airlines ORDER BY name";

        dbService.getPool().query(sql).execute()
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Airline::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    rc.fail(new HttpException(500, "Failed to retrieve airlines"));
                });
    }

    public void searchAirlines(RoutingContext rc) {

        String id = rc.request().getParam("id");
        String name = rc.request().getParam("name");
        String code = rc.request().getParam("code");
        String country = rc.request().getParam("country");

        StringBuilder sql = new StringBuilder("SELECT id, name, code, country FROM airlines WHERE 1=1");

        Tuple params = Tuple.tuple();

        if (id != null) {
            sql.append(" AND id = ?");
            params.addLong(Long.parseLong(id));
        }
        if (name != null) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.addString("%" + name.toLowerCase() + "%");
        }
        if (code != null) {
            sql.append(" AND LOWER(code) LIKE ?");
            params.addString("%" + code.toLowerCase() + "%");
        }
        if (country != null) {
            sql.append(" AND LOWER(country) LIKE ?");
            params.addString("%" + country.toLowerCase() + "%");
        }

        sql.append(" ORDER BY name");

        dbService.getPool().preparedQuery(sql.toString()).execute(params)
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Airline::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
                .onFailure(err -> {
                    rc.fail(new HttpException(500, "Failed to search airlines"));
                });
    }

}
