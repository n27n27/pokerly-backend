package com.rolling.pokerly.gamesession.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class GameSessionRequest {
    private Long venueId;
    private LocalDate playDate;
    private String title;
    private String gameType;
    private BigDecimal buyIn;
    private Integer entries;
    private BigDecimal cashOut;
    private BigDecimal pointUsed;
    private BigDecimal pointRemainAfter;
    private BigDecimal discount;
    private String notes;
}
