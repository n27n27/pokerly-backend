package com.rolling.pokerly.statistics.dto;

public record MonthlySummary(
        long totalSessions,
        long totalBuyIn,
        long totalPrize,
        long totalProfit,
        double roi,          // 퍼센트 (%), 예: 32.5
        long itmCount,
        double itmRatio,     // 0.0 ~ 1.0
        double avgBuyIn,
        double avgPrize      // ITM 세션 기준 평균 프라이즈
) {
}

