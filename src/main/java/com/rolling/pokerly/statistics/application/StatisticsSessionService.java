package com.rolling.pokerly.statistics.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.statistics.dto.StatisticsSessionResponse;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.venue.repo.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsSessionService {

    private final GameSessionRepository sessionRepository;
    private final VenueRepository venueRepository;

    public StatisticsSessionResponse getSessionStats(Long userId) {

        List<GameSession> list = sessionRepository.findByUserId(userId);
        if (list.isEmpty()) {
            return emptyResponse();
        }

        // --------------------------
        // 1) Summary
        // --------------------------
        long totalSessions = list.size();
        long totalBuyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long totalPrize = list.stream().mapToLong(s -> safe(s.getPrize())).sum();
        long totalProfit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        double roi = totalBuyIn == 0 ? 0 : (double) totalProfit / totalBuyIn * 100;

        long itmCount = list.stream().filter(s -> safe(s.getPrize()) > 0).count();
        double itmRatio = (double) itmCount / totalSessions;

        var summary = new StatisticsSessionResponse.Summary(
                totalSessions,
                totalBuyIn,
                totalPrize,
                totalProfit,
                roi,
                itmCount,
                itmRatio
        );

        // --------------------------
        // 2) Type 별
        // --------------------------
        var byType = list.stream()
                .collect(Collectors.groupingBy(GameSession::getSessionType))
                .entrySet()
                .stream()
                .map(e -> buildTypeStat(e.getKey(), e.getValue()))
                .toList();

        // --------------------------
        // 3) 바인 레벨
        // --------------------------
        var byBuyInLevel = list.stream()
                .collect(Collectors.groupingBy(s -> level(s.getTotalBuyIn())))
                .entrySet()
                .stream()
                .map(e -> buildBuyInLevelStat(e.getKey(), e.getValue()))
                .toList();

        // --------------------------
        // 4) 매장별
        // --------------------------
        var byVenue = list.stream()
                .collect(Collectors.groupingBy(GameSession::getVenueId))
                .entrySet()
                .stream()
                .map(e -> buildVenueStat(e.getKey(), e.getValue()))
                .toList();

        // --------------------------
        // 5) ITM 패턴
        // --------------------------
        var itmPattern = buildItmPattern(list);

        // --------------------------
        // 6) 손익 분포
        // --------------------------
        var distribution = buildDistribution(list);

        // --------------------------
        // 7) 컨디션 분석 (일지는 나중에 확장)
        // 일단 빈 데이터로 리턴
        // --------------------------
        var condition = new StatisticsSessionResponse.ConditionAnalysis(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // --------------------------
        // 8) Top / Worst
        // --------------------------
        var top = list.stream()
                .sorted(Comparator.comparing(GameSession::getNetProfit).reversed())
                .limit(3)
                .map(this::simpleSession)
                .toList();

        var worst = list.stream()
                .sorted(Comparator.comparing(GameSession::getNetProfit))
                .limit(3)
                .map(this::simpleSession)
                .toList();

        return new StatisticsSessionResponse(
                summary,
                byType,
                byBuyInLevel,
                byVenue,
                itmPattern,
                distribution,
                condition,
                top,
                worst
        );
    }

    // -------------------------------------------------------------------
    // Helper builders
    // -------------------------------------------------------------------

    private StatisticsSessionResponse.TypeStat buildTypeStat(String type, List<GameSession> list) {
        long sessions = list.size();
        long profit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        long buyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long itm = list.stream().filter(s -> safe(s.getPrize()) > 0).count();

        double roi = buyIn == 0 ? 0 : (double) profit / buyIn * 100;
        double itmRatio = (double) itm / sessions;

        return new StatisticsSessionResponse.TypeStat(
                type,
                sessions,
                buyIn,
                profit,
                roi,
                itm,
                itmRatio
        );
    }

    private StatisticsSessionResponse.BuyInLevelStat buildBuyInLevelStat(String level, List<GameSession> list) {
        long sessions = list.size();
        long profit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        long buyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long itm = list.stream().filter(s -> safe(s.getPrize()) > 0).count();

        double roi = buyIn == 0 ? 0 : (double) profit / buyIn * 100;

        return new StatisticsSessionResponse.BuyInLevelStat(
                level,
                sessions,
                buyIn,
                profit,
                roi,
                itm
        );
    }

    private StatisticsSessionResponse.VenueStat buildVenueStat(Long venueId, List<GameSession> list) {
        String name = venueName(list.get(0));

        long sessions = list.size();
        long profit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        long buyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long itm = list.stream().filter(s -> safe(s.getPrize()) > 0).count();

        double roi = buyIn == 0 ? 0 : (double) profit / buyIn * 100;
        double itmRatio = (double) itm / sessions;

        return new StatisticsSessionResponse.VenueStat(
                venueId,
                name,
                sessions,
                buyIn,
                profit,
                roi,
                itm,
                itmRatio
        );
    }

    private StatisticsSessionResponse.ItmPattern buildItmPattern(List<GameSession> list) {
        int maxItm = 0, maxLose = 0;
        int curItm = 0, curLose = 0;

        for (GameSession s : list.stream()
                .sorted(Comparator.comparing(GameSession::getPlayDate))
                .toList()) {

            if (safe(s.getPrize()) > 0) {
                curItm++;
                maxItm = Math.max(maxItm, curItm);
                curLose = 0;
            } else {
                curLose++;
                maxLose = Math.max(maxLose, curLose);
                curItm = 0;
            }
        }

        return new StatisticsSessionResponse.ItmPattern(maxItm, maxLose);
    }

    private StatisticsSessionResponse.ProfitDistribution buildDistribution(List<GameSession> list) {
        List<Long> profits = list.stream()
                .map(s -> safe(s.getNetProfit()))
                .toList();

        long maxUp = profits.stream().max(Long::compareTo).orElse(0L);
        long maxDown = profits.stream().min(Long::compareTo).orElse(0L);

        double avg = profits.stream().mapToDouble(v -> v).average().orElse(0);
        double variance = profits.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .average().orElse(0);

        return new StatisticsSessionResponse.ProfitDistribution(
                profits,
                Math.sqrt(variance),
                maxUp,
                maxDown
        );
    }

    private StatisticsSessionResponse.SimpleSession simpleSession(GameSession s) {
        long totalBuyIn = safe(s.getTotalBuyIn());
        long profit = safe(s.getNetProfit());
        long prize = safe(s.getPrize());

        double roi = (totalBuyIn == 0)
                ? 0
                : (double) profit / totalBuyIn * 100;

        return new StatisticsSessionResponse.SimpleSession(
                s.getId(),
                s.getPlayDate() != null ? s.getPlayDate().toString() : null,
                totalBuyIn,
                prize,
                profit,
                roi,
                venueName(s),
                s.getSessionType()
        );
    }

    private long safe(Long v) {
        return v == null ? 0 : v;
    }

    private String level(Long buyIn) {
        if (buyIn == null) return "기타";
        if (buyIn < 70_000) return "5-6만";
        if (buyIn < 150_000) return "10만";
        if (buyIn < 300_000) return "20만";
        return "30만+";
    }

    private String venueName(GameSession s) {
        if (s.getVenueId() == null) {
            return "기타";
        }
        return venueRepository.findById(s.getVenueId())
                .map(Venue::getName)
                .orElse("기타");
    }

    private StatisticsSessionResponse emptyResponse() {
        return new StatisticsSessionResponse(
                new StatisticsSessionResponse.Summary(0, 0, 0, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of(),
                new StatisticsSessionResponse.ItmPattern(0, 0),
                new StatisticsSessionResponse.ProfitDistribution(List.of(), 0, 0, 0),
                new StatisticsSessionResponse.ConditionAnalysis(List.of(), List.of(), List.of()),
                List.of(),
                List.of()
        );
    }
}
