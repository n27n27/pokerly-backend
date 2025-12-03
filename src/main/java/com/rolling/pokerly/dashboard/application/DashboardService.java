package com.rolling.pokerly.dashboard.application;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.KpiSection;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.RecentSession;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.RemainingPointVenue;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.SummarySection;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.TrendPoint;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse.VenueStat;
import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.venue.repo.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final GameSessionRepository sessionRepo;
    private final VenueRepository venueRepo;

    // ======================================================
    // 메인 진입점: 월간 대시보드 조회
    // ======================================================
    public DashboardMonthlyResponse getMonthly(Long userId, int year, int month) {

        YearMonth ym = YearMonth.of(year, month);

        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        // 1) 이번 달 세션 조회
        List<GameSession> monthSessions =
                sessionRepo.findByUserIdAndPlayDateBetween(userId, start, end);

        // 2) 최근 3개 세션 (있든 없든 안정적으로 동작)
        List<RecentSession> recentSessions =
                buildRecentSessions(
                        sessionRepo.findTop3ByUserIdOrderByPlayDateDesc(userId)
                );

        // 3) 최근 6개월 추세 (세션이 없어도 0으로 채워진 6개가 내려감)
        List<TrendPoint> trend = buildLast6MonthsTrend(userId, ym);

        // 4) 잔여 포인트 매장 (없으면 빈 리스트)
        List<RemainingPointVenue> remainingPointVenues =
                buildRemainingPointVenues(userId);

        // ✅ 이번 달 세션이 아예 없을 때: KPI / summary / venue 랭킹도 전부 0 / 빈 리스트로 명시
        if (monthSessions.isEmpty()) {
            KpiSection emptyKpi = new KpiSection(0L, 0L, 0L, 0.0);
            SummarySection emptySummary = new SummarySection(0, 0L, 0L, 0L);
            List<VenueStat> emptyVenueStats = List.of();

            return new DashboardMonthlyResponse(
                    year,
                    month,
                    emptyKpi,
                    emptySummary,
                    trend,
                    recentSessions,
                    emptyVenueStats,   // topProfitVenues
                    emptyVenueStats,   // topVisitVenues
                    remainingPointVenues
            );
        }

        // ===== 여기부터는 "이번 달에 세션이 하나 이상 있는 경우" =====

        // 5) venueId → venueName 매핑 (이번 달 세션 기준)
        Map<Long, String> venueNames = loadVenueNames(monthSessions);

        // 6) KPI / Summary 계산
        Totals totals = calcTotals(monthSessions);

        KpiSection kpi = new KpiSection(
                totals.totalProfit(),
                totals.totalBuyIn(),
                totals.totalPrize(),
                totals.totalBuyIn() > 0
                        ? (totals.totalProfit() * 100.0 / totals.totalBuyIn())
                        : 0.0
        );

        SummarySection summary = new SummarySection(
                monthSessions.size(),
                totals.totalBuyIn(),
                totals.totalPrize(),
                totals.totalProfit()
        );

        // 7) 매장별 집계
        List<VenueStat> venueStats = aggregateByVenue(monthSessions, venueNames);

        List<VenueStat> topProfitVenues = venueStats.stream()
                .sorted(Comparator.comparingLong(VenueStat::totalProfit).reversed())
                .limit(3)
                .toList();

        List<VenueStat> topVisitVenues = venueStats.stream()
                .sorted(
                        Comparator.comparingInt(VenueStat::sessionCount).reversed()
                                .thenComparingLong(VenueStat::totalProfit).reversed()
                )
                .limit(3)
                .toList();

        // 최종 반환 DTO
        return new DashboardMonthlyResponse(
                year,
                month,
                kpi,
                summary,
                trend,
                recentSessions,
                topProfitVenues,
                topVisitVenues,
                remainingPointVenues
        );
    }

    // ======================================================
    // 로직 구성 요소들
    // ======================================================

    // 총 buyIn / prize / profit 계산
    private Totals calcTotals(List<GameSession> sessions) {
        long buyIn = 0;
        long prize = 0;

        for (GameSession s : sessions) {
            buyIn += s.getTotalBuyIn();
            prize += s.getPrize();
        }
        return new Totals(buyIn, prize);
    }

    private record Totals(long totalBuyIn, long totalPrize) {
        long totalProfit() { return totalPrize - totalBuyIn; }
    }

    // venueId → venueName 맵 생성
    private Map<Long, String> loadVenueNames(List<GameSession> sessions) {

        Set<Long> venueIds = sessions.stream()
                .map(GameSession::getVenueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (venueIds.isEmpty()) return Map.of();

        List<Venue> venues = venueRepo.findByIdIn(venueIds);

        return venues.stream()
                .collect(Collectors.toMap(Venue::getId, Venue::getName));
    }

    // 최근 3개 세션 + venueName 포함 (세션이 없어도 빈 리스트)
    private List<RecentSession> buildRecentSessions(List<GameSession> sessions) {

        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }

        Map<Long, String> venueNames = loadVenueNames(sessions);

        return sessions.stream()
                .limit(3)
                .map(s -> new RecentSession(
                        s.getId(),
                        s.getPlayDate() != null ? s.getPlayDate().toString() : null,
                        venueNames.getOrDefault(s.getVenueId(), null),
                        s.getGameType(),
                        s.getTotalBuyIn(),
                        s.getPrize(),
                        s.getPrize() - s.getTotalBuyIn()
                ))
                .toList();
    }

    // 최근 6개월 추세 (세션 없어도 6개 모두 0으로 채워서 반환)
    private List<TrendPoint> buildLast6MonthsTrend(Long userId, YearMonth baseYm) {

        YearMonth startYm = baseYm.minusMonths(5);

        LocalDate from = startYm.atDay(1);
        LocalDate to   = baseYm.atEndOfMonth();

        List<GameSession> sessions =
                sessionRepo.findByUserIdAndPlayDateBetweenOrderByPlayDateAsc(userId, from, to);

        if (sessions == null || sessions.isEmpty()) {
            // 그냥 0으로 채운 6개월 리턴
            List<TrendPoint> emptyTrend = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                YearMonth ym = startYm.plusMonths(i);
                emptyTrend.add(new TrendPoint(
                        ym.getYear(),
                        ym.getMonthValue(),
                        0L,
                        0L,
                        0L
                ));
            }
            return emptyTrend;
        }

        // YearMonth → Totals
        Map<YearMonth, Totals> monthlyTotals =
                sessions.stream()
                        .filter(s -> s.getPlayDate() != null) // 혹시 모를 null 방어
                        .collect(Collectors.groupingBy(
                                s -> YearMonth.from(s.getPlayDate()),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        this::calcTotals
                                )
                        ));

        List<TrendPoint> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {

            YearMonth ym = startYm.plusMonths(i);

            Totals t = monthlyTotals.getOrDefault(ym, new Totals(0, 0));

            result.add(new TrendPoint(
                    ym.getYear(),
                    ym.getMonthValue(),
                    t.totalBuyIn(),
                    t.totalPrize(),
                    t.totalProfit()
            ));
        }

        return result;
    }

    // venue 집계 (이번 달 기준)
    private List<VenueStat> aggregateByVenue(List<GameSession> sessions,
                                             Map<Long, String> venueNames) {

        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }

        Map<Long, VenueAgg> map = new HashMap<>();

        for (GameSession s : sessions) {
            Long vId = s.getVenueId();
            if (vId == null) continue;

            VenueAgg agg = map.computeIfAbsent(vId, id -> new VenueAgg());
            agg.sessionCount++;
            agg.totalBuyIn += s.getTotalBuyIn();
            agg.totalPrize += s.getPrize();
        }

        if (map.isEmpty()) {
            return List.of();
        }

        return map.entrySet().stream()
                .map(e -> {
                    Long venueId = e.getKey();
                    VenueAgg v = e.getValue();

                    long profit = v.totalPrize - v.totalBuyIn;

                    return new VenueStat(
                            venueId,
                            venueNames.getOrDefault(venueId, "알 수 없는 매장"),
                            v.sessionCount,
                            v.totalBuyIn,
                            v.totalPrize,
                            profit
                    );
                })
                .toList();
    }

    // 잔여 포인트 매장: 유저가 가진 매장 중 pointBalance > 0 인 것만, 많은 순
    private List<RemainingPointVenue> buildRemainingPointVenues(Long userId) {

        List<Venue> venues = venueRepo.findByCreatedByUserId(userId);
        if (venues == null || venues.isEmpty()) {
            return List.of();
        }

        return venues.stream()
                .filter(v -> v.getPointBalance() != null && v.getPointBalance() > 0)
                .sorted(Comparator.comparingLong(Venue::getPointBalance).reversed())
                .map(v -> new RemainingPointVenue(
                        v.getId(),
                        v.getName(),
                        v.getPointBalance()
                ))
                .toList();
    }

    private static class VenueAgg {
        int sessionCount = 0;
        long totalBuyIn = 0;
        long totalPrize = 0;
    }
}
