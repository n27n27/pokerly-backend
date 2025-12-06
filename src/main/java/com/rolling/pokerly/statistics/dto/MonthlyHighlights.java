package com.rolling.pokerly.statistics.dto;

public record MonthlyHighlights(
        Long bestSessionProfit,   // null 허용 (세션 없으면)
        Long worstSessionProfit,
        Integer maxConsecutiveITM // null 허용
) {
}
