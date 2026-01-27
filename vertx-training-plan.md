# Vert.X Quick Start for Spring WebFlux Developers

**Assessment-Ready in 1-2 Days**

## Overview

Intensive crash course for developers with **solid Spring WebFlux experience** to master Vert.X fundamentals for the airline booking system assessment.

**Target**: Max 10-12 hours (1-2 days) | **Prerequisite**: Spring WebFlux background

---

## Day 1: Core Vert.X Fundamentals (Estimated 6 hours of learning)

### Hour 1-2: Spring WebFlux → Vert.X Mapping

#### Key Differences

| Spring WebFlux                 | Vert.X           | Notes                            |
|--------------------------------|------------------|----------------------------------|
| `@RestController`              | Router + Handler | Programmatic vs annotation-based |
| `Mono/Flux`                    | `Future<T>`      | Different reactive types         |
| `WebClient`                    | `WebClient`      | Similar but different API        |
| `@Autowired`                   | Manual DI        | No Spring container              |
| `ServerRequest/ServerResponse` | `RoutingContext` | Single context object            |
| `application.yml`              | JSON config      | Different config format          |

#### Code Comparison

```java
// Spring WebFlux
@GetMapping("/users/{id}")
public Mono<User> getUser(@PathVariable String id) {
    return userService.findById(id);
}

// Vert.X equivalent
public void getUser(RoutingContext ctx) {
    String id = ctx.pathParam("id");
    userService.findById(id)
        .onSuccess(user -> ctx.response()
            .putHeader("content-type", "application/json")
            .end(Json.encode(user)))
        .onFailure(ctx::fail);
}
```

### Hour 3-4: HTTP Server & Routing

#### Basic Server Setup

```java
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        
        // Routes
        router.get("/airlines").handler(this::getAllAirlines);
        router.post("/airlines").handler(BodyHandler.create()).handler(this::createAirline);
        router.get("/flights/:id").handler(this::getFlightById);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onComplete(startPromise);
    }
}
```

#### Essential Router Operations

- Path params: `ctx.pathParam("id")`
- Query params: `ctx.queryParam("filter")`
- Request body: `ctx.body().asJsonObject()`
- JSON response: `ctx.response().end(Json.encode(object))`

### Hour 5-6: Database Integration

#### Embedded H2 Database Setup

```java
// Connection setup
JDBCConnectOptions options = new JDBCConnectOptions()
    .setJdbcUrl("jdbc:h2:mem:airline_booking")
    .setUser("sa")
    .setPassword("");

JDBCPool pool = JDBCPool.pool(vertx, options, new PoolOptions().setMaxSize(5));

// CRUD Operations
public Future<JsonArray> getAllAirlines() {
    return pool.query("SELECT * FROM airlines")
        .execute()
        .map(rows -> {
            JsonArray result = new JsonArray();
            rows.forEach(row -> result.add(new JsonObject()
                .put("id", row.getLong("id"))
                .put("code", row.getString("code"))
                .put("name", row.getString("name"))));
            return result;
        });
}
```

---

## Day 2: Assessment Implementation (Estimated 6 hours of learning)

### Hour 1-2: Service Layer Pattern

#### Converting Spring Services

```java
// Spring Service → Vert.X Service
public class AirlineService {
    private final JDBCPool pool;
    
    public Future<List<JsonObject>> findAll() {
        return pool.query("SELECT * FROM airlines")
            .execute()
            .map(this::mapToAirlineList);
    }
    
    public Future<JsonObject> findById(Long id) {
        return pool.preparedQuery("SELECT * FROM airlines WHERE id = ?")
            .execute(Tuple.of(id))
            .map(rows -> {
                if (rows.size() == 0) {
                    throw new RuntimeException("Airline not found");
                }
                return mapToJsonObject(rows.iterator().next());
            });
    }
}
```

### Hour 3-4: Assessment Endpoints

#### Various RESTful APIs Endpoints

```java
// Airlines
router.post("/airlines").handler(BodyHandler.create()).handler(this::createAirline);
router.get("/airlines").handler(this::getAllAirlines);

// Flights
router.post("/flights").handler(BodyHandler.create()).handler(this::createFlight);
router.get("/flights/:id").handler(this::getFlightById);
router.get("/flights/search").handler(this::searchFlights); // ?from=X&to=Y

// Passengers
router.post("/passengers").handler(BodyHandler.create()).handler(this::createPassenger);

// Bookings
router.post("/bookings").handler(BodyHandler.create()).handler(this::createBooking);
router.get("/bookings/:id").handler(this::getBookingById);
router.delete("/bookings/:id").handler(this::cancelBooking);
router.get("/passengers/:id/bookings").handler(this::getPassengerBookings);
```

### Hour 5-6: Testing & Final Prep

#### Vert.X Testing

```java
@ExtendWith(VertxExtension.class)
class AirlineHandlerTest {
    @Test
    void testGetAllAirlines(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
            .get(8080, "localhost", "/airlines")
            .send()
            .onComplete(testContext.succeeding(response -> {
                testContext.verify(() -> 
                    assertEquals(200, response.statusCode()));
                testContext.completeNow();
            }));
    }
}
```

#### Error Handling

```java
// Global error handler
router.errorHandler(404, ctx -> {
    ctx.response().setStatusCode(404)
        .putHeader("content-type", "application/json")
        .end("{\"error\":\"Not Found\"}");
});
```

---

## Essential Resources

### Documentation (Bookmarks these)

1. **Vert.X Web Guide**: <https://vertx.io/docs/vertx-web/java/>
2. **JDBC SQL Client Guide**: <https://vertx.io/docs/vertx-jdbc-client/java/>
3. **SQL Client Template Guide**: <https://vertx.io/docs/vertx-sql-client-templates/java/>
4. **JUnit 5 Extension**: <https://vertx.io/docs/vertx-junit5/java/>

### Code Examples

**Official Vert.X Examples GitHub Repository**: <https://github.com/vert-x3/vertx-examples>

- Focus on: Various examples from <https://github.com/vert-x3/vertx-examples?tab=readme-ov-file#running-the-examples> such as `web-examples/`, `sql-client-examples/`, `junit5-examples/`, 

### Alternative Learning Resources

- **Vert.X Official Guides**: <https://vertx.io/get-started/>
- **Baeldung Vert.X Tutorials**: <https://www.baeldung.com/vertx>
- **Red Hat Developer Vert.X Articles**: <https://developers.redhat.com/topics/vertx>

---

## Assessment Success Checklist

### After Day 1

- [ ] Basic Verticle and HTTP server running
- [ ] Router and Handler pattern understood
- [ ] JSON request/response handling
- [ ] Database connection working

### After Day 2 (Assessment Ready)

- [ ] All 10 REST endpoints are implemented
- [ ] Non-blocking database operations
- [ ] Service layer pattern
- [ ] Tests have written and passing
- [ ] Error handling implemented
- [ ] 85%+ test coverage achieved

---

## Quick Commands

```bash
# Run application
mvn compile exec:java -Dexec.mainClass="com.dvtsoftware.airline.booking.MainVerticle"

# Run tests with coverage
mvn clean verify

# Test endpoints
curl http://localhost:8080/airlines
curl -X POST http://localhost:8080/airlines -H "Content-Type: application/json" -d '{"code":"VS","name":"Virgin Atlantic","country":"UK"}'

# Docker
mvn clean package
docker build -t airline-booking .
docker run -p 8080:8080 airline-booking
```

**Success Strategy**: Focus on getting endpoints working first, then add comprehensive error handling and testing. The key is understanding how Vert.X's Future-based approach differs from WebFlux's Mono/Flux patterns.
