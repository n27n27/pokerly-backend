package com.rolling.pokerly.statistics.dto;

import java.time.LocalDate;

public record MonthlyDailyItem(
        LocalDate date,
        long sessionCount,
        long buyIn,
        long prize,
        long profit          // 해당 날짜 합산 netProfit
) {
}
