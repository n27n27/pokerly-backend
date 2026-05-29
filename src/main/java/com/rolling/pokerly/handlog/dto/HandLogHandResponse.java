package com.rolling.pokerly.handlog.dto;

import java.time.LocalDateTime;

import com.rolling.pokerly.handlog.domain.HandLogHand;

public record HandLogHandResponse(
        Long id,
        Long eventId,
        Long blindLevelId,

        String holeCards,
        String hand,

        String firstRank,
        String secondRank,
        Boolean suited,

        String position,

        String actionType,
        String actionLabel,
        Boolean preflopAllIn,

        String resultType,
        String resultLabel,

        Boolean reviewRequired,

        String memo,

        String handStrengthTier,
        String handStrengthLabel,
        String handStrengthColor,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static HandLogHandResponse from(HandLogHand e) {
        return new HandLogHandResponse(
                e.getId(),
                e.getEventId(),
                e.getBlindLevelId(),

                e.getHoleCards(),
                e.getHoleCards(),

                e.getFirstRank(),
                e.getSecondRank(),
                e.getSuited(),

                e.getPosition(),

                e.getActionType(),
                e.getActionLabel(),
                e.getPreflopAllIn(),

                e.getResultType(),
                e.getResultLabel(),

                e.getReviewRequired(),

                e.getMemo(),

                e.getHandStrengthTier(),
                e.getHandStrengthLabel(),
                e.getHandStrengthColor(),

                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}