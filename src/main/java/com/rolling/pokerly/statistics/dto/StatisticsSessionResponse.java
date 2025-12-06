package com.rolling.pokerly.statistics.dto;

import java.util.List;

public record StatisticsSessionResponse(
        Summary summary,
        List<TypeStat> byType,
        List<BuyInLevelStat> byBuyInLevel,
        List<VenueStat> byVenue,
        ItmPattern itmpattern,
        ProfitDistribution profitDistribution,
        ConditionAnalysis conditionAnalysis,
        List<SimpleSession> topSessions,
        List<SimpleSession> worstSessions
) {

    // -----------------------------
    // 1) KPI Summary
    // -----------------------------
    public static record Summary(
            long totalSessions,
            long totalBuyIn,
            long totalPrize,
            long totalProfit,
            double roi,
            long itmCount,
            double itmRatio
    ) {}

    // -----------------------------
    // 2) Session Type Stats
    // 프론트에서:
    // - type
    // - sessions
    // - totalBuyIn
    // - totalProfit
    // - roi
    // - itmRatio
    // 를 사용함
    // -----------------------------
    public static record TypeStat(
            String type,
            long sessions,
            long totalBuyIn,
            long totalProfit,
            double roi,
            long itmCount,
            double itmRatio
    ) {}

    // -----------------------------
    // 3) Buy-In Level Stats
    // 프론트에서:
    // - level
    // - sessions
    // - totalBuyIn
    // - totalProfit
    // - roi
    // - itmCount
    // -----------------------------
    public static record BuyInLevelStat(
            String level,
            long sessions,
            long totalBuyIn,
            long totalProfit,
            double roi,
            long itmCount
    ) {}

    // -----------------------------
    // 4) Venue Stats
    // 프론트에서:
    // - venueId
    // - venueName
    // - sessions
    // - totalBuyIn
    // - totalProfit
    // - roi
    // - itmRatio
    // -----------------------------
    public static record VenueStat(
            Long venueId,
            String venueName,
            long sessions,
            long totalBuyIn,
            long totalProfit,
            double roi,
            long itmCount,
            double itmRatio
    ) {}

    // -----------------------------
    // 5) ITM Pattern
    // -----------------------------
    public static record ItmPattern(
            int maxConsecutiveItm,
            int maxConsecutiveLose
    ) {}

    // -----------------------------
    // 6) Profit Distribution
    // -----------------------------
    public static record ProfitDistribution(
            List<Long> profits,  // 각 세션의 순이익 리스트
            double stddev,
            long maxUp,
            long maxDown
    ) {}

    // -----------------------------
    // 7) Condition Analysis
    // (현재는 빈 데이터 리턴, 나중에 연동 예정)
    // -----------------------------
    public static record ConditionAnalysis(
            List<ConditionEntry> byCondition,
            List<ConditionEntry> byMental,
            List<ConditionEntry> byFatigue
    ) {

        public static record ConditionEntry(
                int score,       // 1~5
                long count,
                long avgProfit,
                double avgRoi
        ) {}
    }

    // -----------------------------
    // 8) Top/Worst Session info
    //
    // 프론트 SessionListCard 기준:
    // - id
    // - playDate (String)
    // - totalBuyIn
    // - prize
    // - netProfit
    // - roi
    // - venueName
    // - sessionType
    // -----------------------------
    public static record SimpleSession(
            Long id,
            String playDate,
            long totalBuyIn,
            long prize,
            long netProfit,
            double roi,
            String venueName,
            String sessionType
    ) {}
}
