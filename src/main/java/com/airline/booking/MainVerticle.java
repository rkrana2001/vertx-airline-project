package com.airline.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.airline.booking.handler.*;
import com.airline.booking.service.DatabaseService;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(com.airline.booking.MainVerticle.class);
    private DatabaseService dbService;

    @Override
    public void start(Promise<Void> startPromise) {
        Integer testPort = config().getInteger("http.port");
        ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
                .addStore(new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("json")
                        .setConfig(new JsonObject().put("path", "src/main/resources/application.json"))));

        retriever.getConfig().onSuccess(fileConfig -> {
            int port;
            if (testPort != null) {
                port = testPort;
            } else {
                // Otherwise use the port from application.json (or 8080 default)
                port = fileConfig.getJsonObject("server", new JsonObject()).getInteger("port", 8080);
            }
            startApp(fileConfig, port, startPromise);
        }).onFailure(err -> {
            if (testPort != null) {
                startApp(new JsonObject(), testPort, startPromise);
            } else {
                startPromise.fail(err);
            }
        });
    }

    private void startApp(JsonObject config, int port, Promise<Void> startPromise) {
        dbService = new DatabaseService(vertx, config.getJsonObject("database"));

        dbService.initialize().onFailure(startPromise::fail).onSuccess(v -> {
            Router router = Router.router(vertx);

            // 1. Global Handlers (Failure handler should be first or last, but BodyHandler must be before POSTs)
            router.route().handler(BodyHandler.create());
            router.route().failureHandler(GlobalFailureHandler::handle);

            // 2. Resource Handlers
            var airlineHandler = new AirlineHandler(dbService);
            var flightHandler = new FlightHandler(dbService);
            var passengerHandler = new PassengerHandler(dbService);
            var bookingHandler = new BookingHandler(dbService);

            // 3. Airline Routes
            router.post("/airlines").handler(airlineHandler::addAirline);
            router.get("/airlines").handler(airlineHandler::listAllAirlines);
            router.get("/airlines/search").handler(airlineHandler::searchAirlines);

            // 4. Flight Routes (The ones that were missing!)
            router.post("/flights").handler(flightHandler::addFlight);
            router.get("/flights/search").handler(flightHandler::searchFlights);
            router.get("/flights/:id").handler(flightHandler::getFlight);
            // Note: If your test hits GET /flights (without search), you might need:
            // router.get("/flights").handler(flightHandler::listAllFlights);

            // 5. Passenger & Booking Routes
            router.post("/passengers").handler(passengerHandler::addPassenger);
            router.get("/passengers/search").handler(passengerHandler::searchPassengers);
            router.post("/bookings").handler(bookingHandler::bookTicket);
            router.get("/bookings/:id").handler(bookingHandler::retrieveBookingDetails);
            router.delete("/bookings/:id").handler(bookingHandler::cancelBooking);
            router.get("/passengers/:id/bookings").handler(bookingHandler::listPassengerBookings);

            // 6. Start Server
            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(port)
                    .onSuccess(server -> {
                        log.info("HTTP server started on port {}", port);
                        startPromise.complete();
                    })
                    .onFailure(err -> {
                        log.error("Failed to start HTTP server", err);
                        startPromise.fail(err);
                    });
        });
    }

    @Override
    public void stop() {
        if (dbService != null) {
            dbService.close();
        }
    }
}
