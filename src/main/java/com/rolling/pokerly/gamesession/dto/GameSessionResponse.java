package com.rolling.pokerly.gamesession.dto;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.rolling.pokerly.gamesession.domain.GameSession;

public record GameSessionResponse(
        Long id,
        Long userId,
        Long venueId,
        String playDate,
        String sessionType,
        String gameType,
        Long buyInPerEntry,
        Integer entries,
        Long discount,
        Long totalBuyIn,
        Long prize,
        Long netProfit,
        String notes,
        Long gtdAmount,
        Integer fieldEntries
) {

    public static GameSessionResponse from(GameSession s) {

        var formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        return new GameSessionResponse(
                s.getId(),
                s.getUserId(),
                s.getVenueId(),
                Optional.ofNullable(s.getPlayDate()).map(d -> d.format(formatter)).orElse(null),
                s.getSessionType(),
                s.getGameType(),
                s.getBuyInPerEntry(),
                s.getEntries(),
                s.getDiscount(),
                s.getTotalBuyIn(),
                s.getPrize(),
                s.getNetProfit(),
                s.getNotes(),
                s.getGtdAmount(),
                s.getFieldEntries()
        );
    }
}
