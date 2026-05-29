package com.rolling.pokerly.handlog.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.rolling.pokerly.handlog.domain.HandLogBlindLevel;

public record HandLogBlindLevelResponse(
        Long id,
        Long eventId,

        Integer levelNo,
        Integer smallBlind,
        Integer bigBlind,
        Integer ante,

        int handCount,
        int reviewRequiredCount,

        List<HandLogHandResponse> hands,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static HandLogBlindLevelResponse from(
            HandLogBlindLevel e,
            List<HandLogHandResponse> hands
    ) {
        var safeHands = Objects.requireNonNullElse(hands, List.<HandLogHandResponse>of());

        int handCount = safeHands.size();

        int reviewRequiredCount = (int) safeHands.stream()
                .filter(hand -> Boolean.TRUE.equals(hand.reviewRequired()))
                .count();

        return new HandLogBlindLevelResponse(
                e.getId(),
                e.getEventId(),

                e.getLevelNo(),
                e.getSmallBlind(),
                e.getBigBlind(),
                e.getAnte(),

                handCount,
                reviewRequiredCount,

                safeHands,

                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}