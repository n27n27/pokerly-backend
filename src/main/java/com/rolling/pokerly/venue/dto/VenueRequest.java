package com.rolling.pokerly.venue.dto;
public record VenueRequest(
        String name,
        String location,
        String notes,
        Long pointBalance
) {
}
