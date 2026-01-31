package com.airline.booking;

import com.airline.booking.handler.AirlineHandler;
import com.airline.booking.handler.BookingHandler;
import com.airline.booking.handler.FlightHandler;
import com.airline.booking.handler.GlobalFailureHandler;
import com.airline.booking.handler.PassengerHandler;
import com.airline.booking.service.DatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    private DatabaseService dbService;

    @Override
    public void start(Promise<Void> startPromise) {

        dbService = new DatabaseService(vertx);

        dbService.initialize()
                .onFailure(startPromise::fail)
                .onSuccess(v -> {
                    Router router = Router.router(vertx);
                    router.route().handler(BodyHandler.create());

                    var airlineHandler = new AirlineHandler(dbService);
                    router.post("/airlines").handler(airlineHandler::addAirline);
                    router.get("/airlines").handler(airlineHandler::listAllAirlines);
                    router.get("/airlines/search").handler(airlineHandler::searchAirlines);

                    var flightHandler = new FlightHandler(dbService);
                    router.post("/flights").handler(flightHandler::addFlight);
                    //router.get("/flights/searchDepDate").handler(flightHandler::searchFlightsDepDate);
                    router.get("/flights/search").handler(flightHandler::searchFlights);
                    router.get("/flights/:id").handler(flightHandler::getFlight);


                    var passengerHandler = new PassengerHandler(dbService);
                    router.post("/passengers").handler(passengerHandler::addPassenger);
                    router.get("/passengers/search").handler(passengerHandler::searchPassengers);


                    var bookingHandler = new BookingHandler(dbService);
                    router.post("/bookings").handler(bookingHandler::bookTicket);
                    router.get("/bookings/:id").handler(bookingHandler::retrieveBookingDetails);
                    router.delete("/bookings/:id").handler(bookingHandler::cancelBooking);
                    router.get("/passengers/:id/bookings").handler(bookingHandler::listPassengerBookings);

                    router.route().failureHandler(GlobalFailureHandler::handle);

                    vertx.createHttpServer()
                            .requestHandler(router)
                            .listen(8080)
                            .onSuccess(server -> {
                                System.out.println("HTTP server started on port 8080");
                                startPromise.complete();
                            })
                            .onFailure(err -> {
                                startPromise.fail(err);
                            });
                });
    }
}
