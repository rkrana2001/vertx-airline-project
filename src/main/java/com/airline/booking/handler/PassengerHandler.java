  package com.airline.booking.handler;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import com.airline.booking.model.Passenger;
  import com.airline.booking.service.DatabaseService;
  import io.vertx.core.Future;
  import io.vertx.core.json.Json;
  import io.vertx.core.json.JsonObject;
  import io.vertx.ext.web.RoutingContext;
  import io.vertx.ext.web.handler.HttpException;
  import io.vertx.sqlclient.Tuple;

  import java.util.ArrayList;
  import java.util.List;

  public class PassengerHandler {

    private static final Logger log = LoggerFactory.getLogger(com.airline.booking.handler.PassengerHandler.class);
    private final DatabaseService dbService;

    public PassengerHandler(DatabaseService dbService) {
      this.dbService = dbService;
    }

    public void addPassenger(RoutingContext rc) {
      JsonObject body = rc.body().asJsonObject();

      // 1. Validation - Tightened to check for email too as per your log message
      if (body == null || isInvalid(body)) {
        rc.fail(new HttpException(400, "Name, Email, and Passport Number are required."));
        return;
      }

      String passport = body.getString("passportNumber");
      String fullName = body.getString("name");
      String email = body.getString("email");
      String[] parts = fullName.trim().split(" ", 2);
      String firstName = parts[0];
      String lastName = (parts.length > 1) ? parts[1] : "";

      dbService.getPool().preparedQuery("SELECT id FROM Passengers WHERE passport_number = ?")
              .execute(Tuple.of(passport))
              .compose(rows -> {
                if (rows.iterator().hasNext()) {
                  return Future.failedFuture(new HttpException(409, "Passenger with this passport already exists."));
                }
                String sql = "INSERT INTO Passengers (first_name, last_name, email, passport_number) VALUES (?, ?, ?, ?)";
                return dbService.getPool().preparedQuery(sql)
                        .execute(Tuple.of(firstName, lastName, email, passport));
              })
              .compose(result -> {
                // TIGHTENED: Safe extraction of generated key
                try {
                  Long generatedId = result.property(io.vertx.jdbcclient.JDBCPool.GENERATED_KEYS).getLong(0);
                  return dbService.getPool().preparedQuery("SELECT * FROM Passengers WHERE id = ?")
                          .execute(Tuple.of(generatedId));
                } catch (Exception e) {
                  return Future.failedFuture(new HttpException(500, "Failed to retrieve saved passenger record."));
                }
              })
              .map(rows -> rows.iterator().hasNext() ? Passenger.fromRow(rows.iterator().next()) : null)
              .onSuccess(p -> {
                if (p == null) rc.fail(new HttpException(500, "Passenger record not found after save."));
                else rc.response().setStatusCode(201).putHeader("Content-Type", "application/json").end(Json.encodePrettily(p));
              })
              .onFailure(rc::fail);
    }

    public void searchPassengers(RoutingContext rc) {
      String passport = rc.queryParam("passportNumber").stream().findFirst().orElse(null);
      String email = rc.queryParam("email").stream().findFirst().orElse(null);

      // TIGHTENED: If no search params are provided, return 400 instead of scanning the whole table
      if (passport == null && email == null) {
        rc.fail(new HttpException(400, "At least one search parameter (passportNumber or email) is required."));
        return;
      }

      StringBuilder sql = new StringBuilder("SELECT * FROM Passengers WHERE 1=1");
      List<Object> paramsList = new ArrayList<>();

      if (passport != null && !passport.isBlank()) {
        sql.append(" AND passport_number = ?");
        paramsList.add(passport.trim());
      }
      if (email != null && !email.isBlank()) {
        sql.append(" AND email = ?");
        paramsList.add(email.trim());
      }

      dbService.getPool().preparedQuery(sql.toString())
              .execute(Tuple.from(paramsList))
              .map(rows -> {
                List<Passenger> list = new ArrayList<>();
                rows.forEach(row -> list.add(Passenger.fromRow(row)));
                return list;
              })
              .onSuccess(list -> rc.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(list)))
              .onFailure(err -> {
                log.error("Passenger search failed", err);
                rc.fail(new HttpException(500, "Internal error during passenger search."));
              });
    }
    private boolean isInvalid(JsonObject body) {
      return !body.containsKey("name") || body.getString("name").isBlank() ||
              !body.containsKey("passportNumber") || body.getString("passportNumber").isBlank() ||
              !body.containsKey("email") || body.getString("email").isBlank();
    }
  }
