package com.airline.booking.model;

import io.vertx.sqlclient.Row;

public record Flight(
        Long id,
        Long airlineId,
        String flightNumber,
        String origin,
        String destination,
        String departure, // Change these to String
        String arrival,   // Change these to String
        Integer seatsAvailable,
        Double price
) {
  public static Flight fromRow(Row r) {
    return new Flight(
            r.getLong("ID"),
            r.getLong("AIRLINE_ID"),
            r.getString("FLIGHT_NUMBER"),
            r.getString("DEPARTURE_AIRPORT"),
            r.getString("ARRIVAL_AIRPORT"),
            r.getLocalDateTime("DEPARTURE_TIME").toString(), // Convert here
            r.getLocalDateTime("ARRIVAL_TIME").toString(),   // Convert here
            r.getInteger("AVAILABLE_SEATS"),
            r.getDouble("PRICE")
    );
  }
}

