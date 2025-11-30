package com.rolling.pokerly.venue.dto;

import com.rolling.pokerly.venue.domain.Venue;
public record VenueResponse(
        Long id,
        String name,
        String location,
        String notes,
        Long pointBalance
) {
    public static VenueResponse from(Venue v) {
        return new VenueResponse(
                v.getId(),
                v.getName(),
                v.getLocation(),
                v.getNotes(),
                v.getPointBalance()
        );
    }
}
