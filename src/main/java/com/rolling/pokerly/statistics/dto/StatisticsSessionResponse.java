package com.rolling.pokerly.statistics.dto;

import java.util.List;

public record StatisticsSessionResponse(
        Summary summary,
        List<TypeStat> byType,
        ItmPattern itmpattern,
        ProfitDistribution profitDistribution,
        ConditionAnalysis conditionAnalysis,
        List<SimpleSession> topSessions,
        List<SimpleSession> worstSessions
) {

    // 요약
    public record Summary(
            long totalSessions,
            long totalBuyIn,
            long totalPrize,
            long totalProfit,
            double roi,
            long itmCount,
            double itmRatio
    ) { }

    // 타입별 성과 (VENUE / MAJOR / ONLINE ...)
    public record TypeStat(
            String type,
            long sessions,
            long totalBuyIn,
            long totalProfit,
            double roi,
            long itmCount,
            double itmRatio
    ) { }
    // ITM 패턴
    public record ItmPattern(
            int maxConsecutiveItm,
            int maxConsecutiveLose
    ) { }

    // 손익 분포
    public record ProfitDistribution(
            List<Long> profits,
            double stddev,
            long maxUp,
            long maxDown
    ) { }

    // 컨디션 분석
    public record ConditionAnalysis(
            List<ConditionEntry> byCondition,
            List<ConditionEntry> byMental,
            List<ConditionEntry> byFatigue
    ) {
        public record ConditionEntry(
                int score,
                long count,
                long avgProfit,
                double avgRoi
        ) { }
    }

    // Top / Worst 세션용 간단 카드
    public record SimpleSession(
            Long id,
            String playDate,
            long totalBuyIn,
            long prize,
            long netProfit,
            double roi,
            String venueName,
            String sessionType,
            boolean isCollab,
            String collabLabel
    ) { }
}
