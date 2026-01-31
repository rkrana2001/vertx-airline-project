  package com.airline.booking.handler;

  import com.airline.booking.model.Passenger;
  import com.airline.booking.service.DatabaseService;
  import io.vertx.core.json.Json;
  import io.vertx.core.json.JsonObject;
  import io.vertx.ext.web.RoutingContext;
  import io.vertx.sqlclient.Tuple;

  public class PassengerHandler {

    private final DatabaseService dbService;

    public PassengerHandler(DatabaseService dbService) {
      this.dbService = dbService;
    }

    public void addPassenger(RoutingContext rc) {
      JsonObject body = rc.body().asJsonObject();

      // Debug: Print the body to the console to see what actually arrived
      System.out.println("Received Body: " + (body != null ? body.encode() : "NULL"));

      if (body == null || !body.containsKey("name") || body.getString("name") == null) {
        rc.fail(400, new Exception("Field 'name' is required (e.g., 'John Doe')"));
        return;
      }

      String fullName = body.getString("name");
      String[] parts = fullName.split(" ", 2); // Split "Roopesh Rana" into ["Roopesh", "Rana"]
      String firstName = parts[0];
      String lastName = (parts.length > 1) ? parts[1] : ""; // Handle case with no last name

      String sql = "INSERT INTO Passengers (first_name, last_name, email, passport_number) VALUES (?, ?, ?, ?)";
      Tuple params = Tuple.of(
              firstName,
              lastName,
              body.getString("email"),
              body.getString("passportNumber") // Make sure your DB column allows NULL if this is missing
      );

      dbService.getPool().preparedQuery(sql)
              .execute(params)
              .compose(rows -> {
                // Modern H2/Vert.x way to get the last inserted ID
                Long generatedId = rows.property(io.vertx.jdbcclient.JDBCPool.GENERATED_KEYS).getLong(0);
                return dbService.getPool().preparedQuery("SELECT * FROM Passengers WHERE id = ?")
                        .execute(Tuple.of(generatedId));
              })
              .map(rows -> rows.iterator().hasNext() ? Passenger.fromRow(rows.iterator().next()) : null)
              .onSuccess(p -> rc.response()
                      .setStatusCode(201)
                      .putHeader("Content-Type", "application/json")
                      .end(Json.encodePrettily(p)))
              .onFailure(err -> {
                err.printStackTrace();
                rc.fail(500, err);
              });
    }

    public void searchPassengers(RoutingContext rc) {
      // 1. Extract query parameters
      String id = rc.queryParam("id").stream().findFirst().orElse(null);
      String firstName = rc.queryParam("firstName").stream().findFirst().orElse(null);
      String lastName = rc.queryParam("lastName").stream().findFirst().orElse(null);
      String passport = rc.queryParam("passportNumber").stream().findFirst().orElse(null);
      String email = rc.queryParam("email").stream().findFirst().orElse(null);

      // 2. Build Dynamic SQL
      StringBuilder sql = new StringBuilder("SELECT * FROM Passengers WHERE 1=1");
      Tuple params = Tuple.tuple();

      if (id != null && !id.isBlank()) {
        sql.append(" AND id = ?");
        params.addLong(Long.valueOf(id));
      }
      if (firstName != null && !firstName.isBlank()) {
        sql.append(" AND FIRST_NAME LIKE ?");
        params.addString("%" + firstName + "%"); // Partial match
      }
      if (lastName != null && !lastName.isBlank()) {
        sql.append(" AND LAST_NAME LIKE ?");
        params.addString("%" + lastName + "%"); // Partial match
      }
      if (passport != null && !passport.isBlank()) {
        sql.append(" AND PASSPORT_NUMBER = ?");
        params.addString(passport);
      }
      if (email != null && !email.isBlank()) {
        sql.append(" AND EMAIL = ?");
        params.addString(email);
      }

      // 3. Execute Query
      dbService.getPool().preparedQuery(sql.toString())
              .execute(params)
              .map(rows -> {
                java.util.List<Passenger> list = new java.util.ArrayList<>();
                rows.forEach(row -> list.add(Passenger.fromRow(row)));
                return list;
              })
              .onSuccess(list -> rc.response()
                      .putHeader("Content-Type", "application/json")
                      .end(Json.encodePrettily(list)))
              .onFailure(err -> {
                err.printStackTrace();
                rc.fail(500, err);
              });
    }

  }
