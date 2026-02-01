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
class PassengerApiTest {

    private WebClient client;

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext ctx) {
        client = WebClient.create(vertx);
        vertx.deployVerticle(new MainVerticle())
                .onComplete(ctx.succeedingThenComplete());
    }

    @Test
    @Order(1)
    @DisplayName("POST /passengers - Should create passenger and split names")
    void addPassenger_shouldReturn201(VertxTestContext ctx) {
        JsonObject newPassenger = new JsonObject()
                .put("name", "Roopesh Rana")
                .put("email", "roopesh@example.com")
                .put("passportNumber", "A1234567");

        client.post(8080, "localhost", "/passengers")
                .sendJsonObject(newPassenger)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(201, resp.statusCode());
                    JsonObject body = resp.bodyAsJsonObject();

                    // Verify the name splitting logic from your handler
                    assertEquals("Roopesh", body.getString("firstName"));
                    assertEquals("Rana", body.getString("lastName"));
                    assertNotNull(body.getInteger("id"));
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("Search Passenger: Create then Search")
    void searchPassengers_shouldWork(VertxTestContext ctx) {
        JsonObject newPassenger = new JsonObject()
                .put("name", "Roopesh Rana")
                .put("email", "roopesh@example.com")
                .put("passportNumber", "A1234567");

        // 1. First, create the passenger
        client.post(8080, "localhost", "/passengers")
                .sendJsonObject(newPassenger)
                .onComplete(ctx.succeeding(postResp -> {
                    assertEquals(201, postResp.statusCode());

                    // 2. ONLY AFTER creation succeeds, search for them
                    client.get(8080, "localhost", "/passengers/search")
                            .addQueryParam("email", "roopesh@example.com")
                            .send()
                            .onComplete(ctx.succeeding(getResp -> ctx.verify(() -> {
                                assertEquals(200, getResp.statusCode());
                                io.vertx.core.json.JsonArray results = getResp.bodyAsJsonArray();

                                // This should now be false (meaning list is NOT empty)
                                assertFalse(results.isEmpty(), "Passenger should be found in search results");
                                assertEquals("Roopesh", results.getJsonObject(0).getString("firstName"));
                                ctx.completeNow();
                            })));
                }));
    }

    @Test
    @DisplayName("POST /passengers - Should fail (400) if name is missing")
    void addPassenger_noName_shouldReturn400(VertxTestContext ctx) {
        JsonObject invalid = new JsonObject().put("email", "no-name@example.com");

        client.post(8080, "localhost", "/passengers")
                .sendJsonObject(invalid)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(400, resp.statusCode());
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("GET /passengers/search - Should return empty list for unknown passport")
    void searchPassengers_noMatch(VertxTestContext ctx) {
        client.get(8080, "localhost", "/passengers/search")
                .addQueryParam("passportNumber", "NON-EXISTENT")
                .send()
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertEquals(200, resp.statusCode());
                    assertEquals(0, resp.bodyAsJsonArray().size());
                    ctx.completeNow();
                })));
    }
}
