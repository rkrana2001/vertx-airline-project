package com.airline.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Vertx;

public class AppLauncher {
    private static final Logger log = LoggerFactory.getLogger(com.airline.booking.MainVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id ->
                        log.info("MainVerticle deployed successfully: {}", id)
                )
                .onFailure(err ->
                        log.error("Verticle deployment failed ", err)
                );
    }
}
