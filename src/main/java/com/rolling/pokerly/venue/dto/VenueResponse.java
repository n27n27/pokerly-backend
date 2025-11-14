package com.rolling.pokerly.venue.dto;

import java.time.LocalDateTime;

import com.rolling.pokerly.venue.domain.Venue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueResponse {

    private Long id;
    private String name;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VenueResponse from(Venue v) {
        return VenueResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .location(v.getLocation())
                .notes(v.getNotes())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
