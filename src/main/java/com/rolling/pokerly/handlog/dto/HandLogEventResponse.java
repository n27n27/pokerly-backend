package com.rolling.pokerly.handlog.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.rolling.pokerly.handlog.domain.HandLogEvent;

public record HandLogEventResponse(
        Long id,

        String name,
        LocalDateTime eventAt,
        Long venueId,

        int handCount,
        int reviewRequiredCount,
        int levelCount,

        List<HandLogBlindLevelResponse> blindLevels,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static HandLogEventResponse from(
            HandLogEvent e,
            List<HandLogBlindLevelResponse> blindLevels
    ) {
        var levels = Objects.requireNonNullElse(blindLevels, List.<HandLogBlindLevelResponse>of());

        int levelCount = levels.size();

        int handCount = levels.stream()
                .mapToInt(HandLogBlindLevelResponse::handCount)
                .sum();

        int reviewRequiredCount = levels.stream()
                .mapToInt(HandLogBlindLevelResponse::reviewRequiredCount)
                .sum();

        return new HandLogEventResponse(
                e.getId(),

                e.getName(),
                e.getEventAt(),
                e.getVenueId(),

                handCount,
                reviewRequiredCount,
                levelCount,

                levels,

                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}