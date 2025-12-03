package com.rolling.pokerly.dashboard.dto;

import java.util.List;

/**
 * Pokerly 대시보드 - 월간 대시보드 응답 DTO
 *
 * 단위:
 *  - 모든 금액(long): 원 단위
 *  - 퍼센트(double): 0~100 (%)
 *
 * 세션 단위 데이터:
 *  - totalBuyIn: 해당 세션의 총 바인 금액 (예: buyInPerEntry * entries)
 *  - prize: 해당 세션의 프라이즈(실제 수령 기준)
 *  - profit: prize - totalBuyIn
 *
 * 포인트:
 *  - 세션 기준 포인트 집계는 없음
 *  - "잔여 포인트 매장" 섹션에서만 venue 단위 남은 포인트를 노출
 */
public record DashboardMonthlyResponse(
        int year,
        int month,
        KpiSection kpis,
        SummarySection summary,
        List<TrendPoint> last6Months,
        List<RecentSession> recentSessions,
        List<VenueStat> topProfitVenues,
        List<VenueStat> topVisitVenues,
        List<RemainingPointVenue> remainingPointVenues // 👈 4) 잔여 포인트 매장(전체)
) {

    /** 상단 KPI 4칸 */
    public static record KpiSection(
            long totalProfit,   // 이번 달 총 이익 (Σ profit)
            long totalBuyIn,    // 이번 달 총 바인 금액 (Σ totalBuyIn)
            long totalPrize,    // 이번 달 총 프라이즈 (Σ prize)
            double roiPercent   // ROI(%) = totalProfit / totalBuyIn * 100
    ) {
    }

    /** 이번 달 상세 요약 */
    public static record SummarySection(
            int totalSessions,  // 이번 달 세션 수
            long totalBuyIn,    // 총 바인
            long totalPrize,    // 총 프라이즈
            long totalProfit    // 총 이익
    ) {
    }

    /** 최근 6개월 손익 추세 */
    public static record TrendPoint(
            int year,
            int month,
            long totalBuyIn,
            long totalPrize,
            long profit
    ) {
    }

    /** 최근 세션 3개 리스트 */
    public static record RecentSession(
            long id,
            String playDate,   // yyyy-MM-dd
            String venueName,
            String gameType,
            long totalBuyIn,
            long prize,
            long profit
    ) {
    }

    /** 매장 랭킹 (수익 TOP / 방문 TOP) */
    public static record VenueStat(
            long venueId,
            String venueName,
            int sessionCount,
            long totalBuyIn,
            long totalPrize,
            long totalProfit
    ) {
    }

    /**
     * 4) 🏆 잔여 포인트 매장(전체 표시)
     *
     * - 포인트가 남아 있는 모든 매장
     * - 포인트 많은 순으로 정렬
     * - 프론트에서는 가로 스크롤 카드 리스트로 렌더링
     */
    public static record RemainingPointVenue(
            long venueId,
            String venueName,
            long remainingPoint // 이 매장에 남아 있는 포인트(원 단위 or 포인트 단위, 네가 기준만 정하면 됨)
    ) {
    }
}
