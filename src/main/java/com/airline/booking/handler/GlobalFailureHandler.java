package com.airline.booking.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.airline.booking.error.ErrorResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

public class GlobalFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(
            com.airline.booking.handler.GlobalFailureHandler.class);

    public static void handle(RoutingContext rc) {
        Throwable failure = rc.failure();
        // Default to 500 if no status code is set
        int statusCode = rc.statusCode() > 0 ? rc.statusCode() : 500;

        String clientMessage;

        if (failure instanceof HttpException httpEx) {
            statusCode = httpEx.getStatusCode();
            clientMessage = httpEx.getPayload() != null ? httpEx.getPayload() : httpEx.getMessage();
        } else if (statusCode < 500 && failure != null) {
            // For 4xx errors, it is usually safe to show the message (e.g., "Invalid input")
            clientMessage = failure.getMessage();
        } else {
            // For 500 errors, hide the raw message from the user!
            clientMessage = "An unexpected error occurred. Please try again later.";
        }

        // Always log the ACTUAL raw error for the developers to see in the console
        if (statusCode >= 500) {
            log.error("CRITICAL ERROR: {} {} | Internal Message: {}",
                    rc.request().method(), rc.request().path(),
                    failure != null ? failure.getMessage() : "Unknown", failure);
        } else {
            log.warn("Client Error: {} {} -> {}", rc.request().method(), rc.request().path(), clientMessage);
        }

        // Use your ErrorResponse record for consistency
        ErrorResponse error = new ErrorResponse(statusCode, clientMessage, rc.request().path());

        if (!rc.response().ended()) {
            rc.response()
                    .setStatusCode(statusCode)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(error).encode());
        }
    }
}
