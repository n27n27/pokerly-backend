package com.rolling.pokerly.statistics.dto;

import java.util.List;

public record VenueStatsResponse(
        SummarySection summary,
        List<VenueStat> venues,
        TopVenueSection topVenues
) {

    public record SummarySection(
            int totalSessions,
            long totalBuyIn,
            long totalPrize,
            long totalProfit,
            double roi,      // 전체 ROI(%)
            int totalVenues
    ) {}

    public record VenueStat(
            Long venueId,
            String venueName,
            int sessions,          // 세션 수
            long totalBuyIn,
            long totalPrize,
            long totalProfit,
            double roi,            // 매장별 ROI(%)
            int itmCount,          // prize > 0 인 세션 수
            double itmRatio,       // itmCount / sessions
            Integer avgEntry,      // 엔트리 기록이 있는 세션 기준 평균, 없으면 null
            int entrySampleCount   // 엔트리 기록이 있는 세션 수 (분모)
    ) {}

    public record TopVenueSection(
            VenueRank bestByProfit,   // 누적 수익 1위 매장
            VenueRank worstByProfit,  // 누적 수익 최하위 매장
            VenueRank bestByRoi       // ROI 1위 매장(분모>0)
    ) {}

    public record VenueRank(
            Long venueId,
            String venueName,
            long totalProfit,
            double roi
    ) {}
}
