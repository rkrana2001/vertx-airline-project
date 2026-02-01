package com.airline.booking.api;

import com.airline.booking.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class BookingApiTest {

    private WebClient client;
    private static final int TEST_PORT = 8888;

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        client = WebClient.create(vertx);
        JsonObject testConfig = new JsonObject().put("http.port", 8888);

        // DeploymentOptions is the key to passing the port to the Verticle
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfig);

        vertx.deployVerticle(new MainVerticle(), options)
                .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @DisplayName("Should successfully create a passenger and then book a ticket")
    void testBookTicketSuccess(Vertx vertx, VertxTestContext testContext) {
        JsonObject passenger = new JsonObject()
                .put("name", "Test User")
                .put("email", "test@dvt.com")
                .put("passportNumber", "ABC12345");

        client.post(TEST_PORT, "localhost", "/passengers")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(passenger)
                .compose(res -> {
                    // FIX: Define passengerId here by extracting it from the response body
                    // Check if your API returns the ID as "id" or "passengerId"
                    Integer pId = res.body().getInteger("id");

                    JsonObject bookingRequest = new JsonObject()
                            .put("flightId", 1)
                            .put("passengerId", pId)
                            .put("seatNumber", "Seat-" + System.currentTimeMillis() % 10000);

                    return client.post(TEST_PORT, "localhost", "/bookings")
                            .as(BodyCodec.jsonObject())
                            .sendJsonObject(bookingRequest);
                })
                .onComplete(testContext.succeeding(res -> testContext.verify(() -> {
                    assertEquals(201, res.statusCode());
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("Should return 400 when flightId is missing")
    void testBookTicketMissingParams(Vertx vertx, VertxTestContext testContext) {
        JsonObject invalidBody = new JsonObject().put("passengerId", 1);

        client.post(TEST_PORT, "localhost", "/bookings")
                .sendJsonObject(invalidBody)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("Should return 404 for a non-existent booking")
    void testRetrieveBookingNotFound(Vertx vertx, VertxTestContext testContext) {
        // Since we DELETE FROM bookings in @BeforeEach, ID 999 definitely won't exist
        client.get(TEST_PORT, "localhost", "/bookings/999")
                .send()
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(404, response.statusCode());
                    testContext.completeNow();
                })));
    }
}
