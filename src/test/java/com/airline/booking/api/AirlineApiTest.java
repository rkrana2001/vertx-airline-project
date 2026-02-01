package com.airline.booking.api;

import com.airline.booking.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class AirlineApiTest {

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext ctx) {
        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> ctx.completeNow())
                .onFailure(ctx::failNow);
    }


    @Test
    void getAirlines_shouldReturn200(Vertx vertx, VertxTestContext ctx) {

        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, 8080, "localhost", "/airlines")
                .onSuccess(req ->
                        req.send()
                                .onSuccess(resp -> {
                                    assertEquals(200, resp.statusCode());
                                    ctx.completeNow();
                                })
                                .onFailure(ctx::failNow)
                )
                .onFailure(ctx::failNow);
    }
}

