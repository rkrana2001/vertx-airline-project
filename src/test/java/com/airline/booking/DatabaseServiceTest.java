package com.airline.booking;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject; // Add this import
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airline.booking.service.DatabaseService;

@ExtendWith(VertxExtension.class)
class DatabaseServiceTest {

    @Test
    void initialize_shouldLoadSchemaAndData(Vertx vertx, VertxTestContext ctx) {
        // Pass an empty JsonObject to satisfy the new constructor
        DatabaseService db = new DatabaseService(vertx, new JsonObject());

        db.initialize()
                .onSuccess(v -> ctx.completeNow())
                .onFailure(ctx::failNow);
    }
}
