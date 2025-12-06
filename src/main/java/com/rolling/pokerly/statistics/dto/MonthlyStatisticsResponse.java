package com.rolling.pokerly.statistics.dto;

import java.util.List;

public record MonthlyStatisticsResponse(
        int year,
        int month,
        MonthlySummary summary,
        List<MonthlyDailyItem> daily,
        MonthlyHighlights highlights
) {
}

