package com.rolling.pokerly.handlog.dto;

public record HandLogBlindLevelCreateRequest(
        Integer levelNo,
        Integer smallBlind,
        Integer bigBlind,
        Integer ante
) {
}