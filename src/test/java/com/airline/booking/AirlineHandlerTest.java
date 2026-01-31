package com.airline.booking;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class AirlineHandlerTest {

  @Test
  void testDeploy(Vertx vertx, VertxTestContext ctx) {

    vertx.deployVerticle(new MainVerticle())
            .onSuccess(id -> ctx.completeNow())
            .onFailure(ctx::failNow);
  }
}
