package com.airline.booking.handler;

import com.airline.booking.model.Airline;
import com.airline.booking.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AirlineHandler {

    private static final Logger LOG = LoggerFactory.getLogger(com.airline.booking.handler.AirlineHandler.class);

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
                    LOG.info("Successfully added new airline: {} (Code: {}) with ID: {}",
                            saved.name(), saved.code(), generatedId);
                    rc.response().setStatusCode(201).putHeader("Content-Type", "application/json")
                            .end(Json.encodePrettily(saved));
                }).onFailure(err -> {
                    LOG.error("Failed to create airline [{}]: {}", airline.code(), err.getMessage());
                    rc.fail(new HttpException(409, "Failed to create airline"));
                });
    }

    public void listAllAirlines(RoutingContext rc) {
        String sql = "SELECT id, name, code, country FROM airlines ORDER BY name";

        dbService.getPool().query(sql).execute()
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Airline::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> {
                            LOG.info("Successfully retrieved {} airlines", list.size());
                            rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list));
                        })
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
            try {
                params.addLong(Long.parseLong(id));
                sql.append(" AND id = ?");
            } catch (NumberFormatException e) {
                rc.fail(new HttpException(400, "Invalid ID format: must be a number"));
                return;
            }
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.addString("%" + name.trim().toLowerCase() + "%");
        }

        if (code != null && !code.isBlank()) {
            sql.append(" AND LOWER(code) LIKE ?");
            params.addString("%" + code.trim().toLowerCase() + "%");
        }

        if (country != null && !country.isBlank()) {
            sql.append(" AND LOWER(country) LIKE ?");
            params.addString("%" + country.trim().toLowerCase() + "%");
        }

        sql.append(" ORDER BY name");

        dbService.getPool().preparedQuery(sql.toString()).execute(params)
                .map(rows -> StreamSupport.stream(rows.spliterator(), false).map(Airline::fromRow)
                        .collect(Collectors.toList())).onSuccess(
                        list -> {
                            LOG.info("Search query successful. Found {} airlines matching criteria.", list.size());
                            rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list));
                        })
                .onFailure(err -> {
                    LOG.error("Search failed: {}", err.getMessage());
                    rc.fail(new HttpException(500, "Failed to search airlines"));
                });
    }

}

