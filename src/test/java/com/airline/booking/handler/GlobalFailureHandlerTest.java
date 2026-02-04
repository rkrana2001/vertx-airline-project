package com.airline.booking.handler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalFailureHandlerTest {

    @Mock RoutingContext rc;
    @Mock HttpServerRequest request;
    @Mock HttpServerResponse response;

    @BeforeEach
    void setup() {
        when(rc.request()).thenReturn(request);
        when(rc.response()).thenReturn(response);
        when(request.path()).thenReturn("/api/test");
        when(request.method()).thenReturn(io.vertx.core.http.HttpMethod.GET);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
    }

    @Test
    @DisplayName("Should return 500 and generic message for raw RuntimeException")
    void handleUnexpectedError() {
        // Setup: A generic exception occurs
        when(rc.failure()).thenReturn(new RuntimeException("Database exploded!"));
        when(rc.statusCode()).thenReturn(-1); // Default state

        GlobalFailureHandler.handle(rc);

        // Verify: Status is 500 and message is obfuscated for security
        verify(response).setStatusCode(500);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).end(captor.capture());

        JsonObject body = new JsonObject(captor.getValue());
        assertEquals(500, body.getInteger("status")); // Changed from statusCode
        assertEquals("An unexpected error occurred. Please try again later.", body.getString("error"));
    }

    @Test
    @DisplayName("Should return specific status and payload from HttpException")
    void handleHttpException() {
        // Setup: A specific Vert.x HttpException
        String customMessage = "Invalid Token";
        when(rc.failure()).thenReturn(new HttpException(401, customMessage));

        GlobalFailureHandler.handle(rc);

        // Verify: Status is 401 and custom message is preserved
        verify(response).setStatusCode(401);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).end(captor.capture());

        JsonObject body = new JsonObject(captor.getValue());
        assertEquals(401, body.getInteger("status")); // Changed from statusCode
        assertEquals(customMessage, body.getString("error"));
    }

    @Test
    @DisplayName("Should preserve 4xx error messages for client-side errors")
    void handleClientError() {
        // Setup: RoutingContext already has a 400 status set
        when(rc.failure()).thenReturn(new IllegalArgumentException("Invalid email format"));
        when(rc.statusCode()).thenReturn(400);

        GlobalFailureHandler.handle(rc);

        verify(response).setStatusCode(400);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).end(captor.capture());

        JsonObject body = new JsonObject(captor.getValue());
        assertEquals("Invalid email format", body.getString("error"));
    }
}
