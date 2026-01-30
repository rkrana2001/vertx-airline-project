package com.dvtsoftware.airline.booking.model;

import io.vertx.sqlclient.Row;

public class Airline {

  private Long id;
  private String name;
  private String code;
  private String country;

  public Airline(Long id, String name, String code, String country) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.country = country;
  }

  public Airline() {}


  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getCountry() { return country; }
  public void setCountry(String country) { this.country = country; }

  // Convert Row to Airline object
  public static Airline fromRow(Row row) {
    Airline airline = new Airline();
    // H2 returns unquoted columns in UPPERCASE
    airline.setId(row.getLong("ID"));
    airline.setName(row.getString("NAME"));
    airline.setCode(row.getString("CODE"));
    airline.setCountry(row.getString("COUNTRY"));
    return airline;
  }

}
