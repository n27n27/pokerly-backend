package com.rolling.pokerly.gamesession.dto;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.rolling.pokerly.gamesession.domain.GameSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameSessionResponse {

    private Long id;
    private Long userId;
    private Long venueId;
    private String playDate;
    private String gameType;

    private Long buyInPerEntry;
    private Integer entries;
    private Long discount;

    private Long totalBuyIn;
    private Long prize;
    private Long netProfit;
    private String notes;

    public static GameSessionResponse from(GameSession s) {

        var dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        return GameSessionResponse.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .venueId(s.getVenueId())
                .playDate(Optional.ofNullable(s.getPlayDate()).map(d -> d.format(dateFormatter)).orElse(null))
                .gameType(s.getGameType())
                .buyInPerEntry(s.getBuyInPerEntry())
                .entries(s.getEntries())
                .discount(s.getDiscount())
                .totalBuyIn(s.getTotalBuyIn())
                .prize(s.getPrize())
                .netProfit(s.getNetProfit())
                .notes(s.getNotes())
                .build();
    }
}
