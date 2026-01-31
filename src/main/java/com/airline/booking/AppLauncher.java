package com.airline.booking;

import io.vertx.core.Vertx;

public class AppLauncher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id ->
                        System.out.println("MainVerticle deployed successfully: " + id)
                )
                .onFailure(Throwable::printStackTrace);
    }
}
