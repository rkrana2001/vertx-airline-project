package com.airline.booking.model;

import io.vertx.sqlclient.Row;

public record Airline(Long id, String name, String code, String country) {

  public Airline {
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("Airline name is required");
  }

  public static com.airline.booking.model.Airline fromRow(Row row) {
    return new com.airline.booking.model.Airline(
            row.getLong("ID"),
            row.getString("NAME"),
            row.getString("CODE"),
            row.getString("COUNTRY")
    );
  }
}

