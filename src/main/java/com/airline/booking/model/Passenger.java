package com.airline.booking.model;

import io.vertx.sqlclient.Row;

public record Passenger(Long id, String firstName, String lastName, String email, String passportNumber) {
  public static com.airline.booking.model.Passenger fromRow(Row r) {
    // Change "id" to "ID" (and check other fields too)
    return new com.airline.booking.model.Passenger(
            r.getLong("ID"),
            r.getString("FIRST_NAME"),
            r.getString("LAST_NAME"),
            r.getString("EMAIL"),
            r.getString("PASSPORT_NUMBER")
    );
  }
}

