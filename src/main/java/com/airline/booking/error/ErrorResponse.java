package com.airline.booking.error;

public record ErrorResponse(
        int status,
        String error,
        String path
) {}
