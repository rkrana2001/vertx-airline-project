package com.dvtsoftware.airline.booking.error;

public record ErrorResponse(
        int status,
        String error,
        String path
) {}
