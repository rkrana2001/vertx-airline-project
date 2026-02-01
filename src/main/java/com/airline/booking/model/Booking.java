package com.airline.booking.model;

import io.vertx.sqlclient.Row;

// In Booking.java
public record Booking(
        Long id,
        String bookingReference,
        Long passengerId,
        Long flightId,
        String seatNumber,
        String status,
        Double totalAmount // Added field
) {
  public static com.airline.booking.model.Booking fromRow(Row r) {
    return new com.airline.booking.model.Booking(
            r.getLong("ID"),
            r.getString("BOOKING_REFERENCE"),
            r.getLong("PASSENGER_ID"),
            r.getLong("FLIGHT_ID"),
            r.getString("SEAT_NUMBER"),
            r.getString("STATUS"),
            r.getDouble("TOTAL_AMOUNT") // Match your schema
    );
  }
}