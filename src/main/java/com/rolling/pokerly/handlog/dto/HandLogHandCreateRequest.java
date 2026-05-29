package com.rolling.pokerly.handlog.dto;

public record HandLogHandCreateRequest(
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
        String handStrengthColor
) {
}