package com.rolling.pokerly.gamesession.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthSummaryResponse {
    private int year;
    private int month;
    private long count;
    private BigDecimal totalBuyIn;            // Σ(buyIn*entries) - Σ(discount)
    private BigDecimal totalDiscount;         // Σ(discount)
    private BigDecimal totalCashOut;          // Σ(cashOut)
    private BigDecimal totalPointUsed;        // Σ(pointUsed)
    private BigDecimal profitCashRealized;    // Σ(profitCashRealized)
    private BigDecimal profitIncludingPoints; // Σ(profitIncludingPoints)
}
