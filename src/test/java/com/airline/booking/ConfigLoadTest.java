package com.airline.booking;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class) // This allows passing Vertx and VertxTestContext as params
public class ConfigLoadTest {

    @Test
    void testApplicationJsonLoadsCorrectly(Vertx vertx, VertxTestContext testContext) {
        // 1. Point to your config file
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "src/main/resources/application.json"));

        ConfigRetriever retriever = ConfigRetriever.create(vertx,
                new ConfigRetrieverOptions().addStore(fileStore));

        // 2. Attempt to retrieve the config
        retriever.getConfig().onComplete(testContext.succeeding(config -> {
            testContext.verify(() -> {
                // 3. Assert your application.json values
                assertNotNull(config.getJsonObject("server"), "Server config missing");
                assertEquals(8080, config.getJsonObject("server").getInteger("port"));
                assertEquals("sa", config.getJsonObject("database").getString("user"));

                // Tell the test it's finally finished
                testContext.completeNow();
            });
        }));
    }
}