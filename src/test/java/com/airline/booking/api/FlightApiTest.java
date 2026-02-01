package com.airline.booking.api;

import com.airline.booking.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlightApiTest {

    private WebClient client;

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext ctx) {
        client = WebClient.create(vertx);
        vertx.deployVerticle(new MainVerticle())
                .onComplete(ctx.succeedingThenComplete());
    }

    @Test
    @Order(1)
    @DisplayName("POST /flights - Should create a new flight")
    void addFlight_shouldReturn201(VertxTestContext ctx) {
        JsonObject newFlight = new JsonObject()
                .put("flightNumber", "SA123")
                .put("airlineId", 1)
                .put("from", "JNB")
                .put("to", "CPT")
                .put("departureTime", "2026-05-01T10:00:00")
                .put("arrivalTime", "2026-05-01T12:00:00")
                .put("seatsAvailable", 50)
                .put("totalSeats", 150)
                .put("price", 1200.50);

        client.post(8080, "localhost", "/flights")
                .sendJsonObject(newFlight)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(201, resp.statusCode());
                    assertNotNull(resp.bodyAsJsonObject().getInteger("id"));
                    ctx.completeNow();
                })));
    }

    @Test
    @Order(2)
    @DisplayName("GET /flights/search - Should search flights with query params")
    void searchFlights_shouldReturnList(VertxTestContext ctx) {
        client.get(8080, "localhost", "/flights/search")
                .addQueryParam("from", "JNB")
                .addQueryParam("to", "CPT")
                .addQueryParam("departure", "2026-05-01")
                .send()
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(200, resp.statusCode());
                    assertTrue(resp.bodyAsJsonArray().size() >= 0);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("GET /flights/:id - Should return 404 for non-existent flight")
    void getFlight_shouldReturn404(VertxTestContext ctx) {
        client.get(8080, "localhost", "/flights/99999")
                .send()
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(404, resp.statusCode());
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("POST /flights - Should return 400 when fields are missing")
    void addFlight_shouldReturn400(VertxTestContext ctx) {
        JsonObject invalidFlight = new JsonObject().put("flightNumber", "FAIL");

        client.post(8080, "localhost", "/flights")
                .sendJsonObject(invalidFlight)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(400, resp.statusCode());
                    ctx.completeNow();
                })));
    }
}