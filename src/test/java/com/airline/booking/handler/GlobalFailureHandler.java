package com.airline.booking.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalFailureHandler.class);

    public static void handle(RoutingContext rc) {
        Throwable failure = rc.failure();
        int statusCode = rc.statusCode() > 0 ? rc.statusCode() : 500;

        String message = "Internal Server Error";

        if (failure instanceof HttpException httpEx) {
            statusCode = httpEx.getStatusCode();
            message = httpEx.getPayload() != null ? httpEx.getPayload() : httpEx.getMessage();
        } else if (failure != null) {
            message = failure.getMessage();
        }

        if (statusCode >= 500) {
            log.error("Request failed: {} {}", rc.request().method(), rc.request().path(), failure);
        } else {
            log.warn("Request failed: {} {} -> {}", rc.request().method(), rc.request().path(), message);
        }

        JsonObject error = new JsonObject()
                .put("status", statusCode)
                .put("error", message)
                .put("path", rc.request().path());

        if (!rc.response().ended()) {
            rc.response()
                    .setStatusCode(statusCode)
                    .putHeader("Content-Type", "application/json")
                    .end(error.encode());
        }
    }
}
