package com.rolling.pokerly.gamesession.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Long venueId;
    private LocalDate playDate;
    private String title;
    private String gameType;

    private Long totalCashIn;
    private Long totalPointIn;
    private Integer entries;

    private Long cashOut;
    private Long discount;
    private Long earnedPoint;
    private String notes;

    private Long profitCashRealized;
    private Long profitIncludingPoints;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GameSessionResponse from(GameSession s) {
        return GameSessionResponse.builder()
                .id(s.getId())
                .venueId(s.getVenueId())
                .playDate(s.getPlayDate())
                .title(s.getTitle())
                .gameType(s.getGameType())
                .totalCashIn(s.getTotalCashIn())
                .totalPointIn(s.getTotalPointIn())
                .entries(s.getEntries())
                .cashOut(s.getCashOut())
                .discount(s.getDiscount())
                .earnedPoint(s.getEarnedPoint())
                .notes(s.getNotes())
                .profitCashRealized(s.getProfitCashRealized())
                .profitIncludingPoints(s.getProfitIncludingPoints())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
