package com.rolling.pokerly.gamesession.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSessionRequest {

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
}
