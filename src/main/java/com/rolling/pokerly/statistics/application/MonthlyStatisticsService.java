package com.rolling.pokerly.statistics.application;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.statistics.dto.MonthlyDailyItem;
import com.rolling.pokerly.statistics.dto.MonthlyHighlights;
import com.rolling.pokerly.statistics.dto.MonthlyStatisticsResponse;
import com.rolling.pokerly.statistics.dto.MonthlySummary;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonthlyStatisticsService {

    private final GameSessionRepository gameSessionRepository;

    public MonthlyStatisticsResponse getMonthlyStatistics(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<GameSession> sessions = gameSessionRepository
                .findByUserIdAndPlayDateBetweenOrderByPlayDateAsc(userId, start, end);

        MonthlySummary summary = buildSummary(sessions);
        List<MonthlyDailyItem> daily = buildDailyItems(sessions);
        MonthlyHighlights highlights = buildHighlights(sessions);

        return new MonthlyStatisticsResponse(year, month, summary, daily, highlights);
    }

    private MonthlySummary buildSummary(List<GameSession> sessions) {
        long totalSessions = sessions.size();

        long totalBuyIn = sessions.stream()
                .mapToLong(this::getBuyIn)
                .sum();

        long totalPrize = sessions.stream()
                .mapToLong(this::getPrize)
                .sum();

        long totalProfit = sessions.stream()
                .mapToLong(this::getProfit)
                .sum();

        long itmCount = sessions.stream()
                .filter(s -> getPrize(s) > 0L)
                .count();

        double roi = 0.0;
        if (totalBuyIn > 0L) {
            roi = totalProfit * 100.0 / totalBuyIn; // %
        }

        double itmRatio = 0.0;
        if (totalSessions > 0L) {
            itmRatio = itmCount * 1.0 / totalSessions;
        }

        double avgBuyIn = 0.0;
        if (totalSessions > 0L) {
            avgBuyIn = totalBuyIn * 1.0 / totalSessions;
        }

        double avgPrize = 0.0;
        if (itmCount > 0L) {
            long totalItmPrize = sessions.stream()
                    .filter(s -> getPrize(s) > 0L)
                    .mapToLong(this::getPrize)
                    .sum();

            avgPrize = totalItmPrize * 1.0 / itmCount;
        }

        return new MonthlySummary(
                totalSessions,
                totalBuyIn,
                totalPrize,
                totalProfit,
                roi,
                itmCount,
                itmRatio,
                avgBuyIn,
                avgPrize
        );
    }

    private List<MonthlyDailyItem> buildDailyItems(List<GameSession> sessions) {
        Map<LocalDate, List<GameSession>> byDate = sessions.stream()
                .collect(Collectors.groupingBy(GameSession::getPlayDate));

        return byDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<GameSession> list = entry.getValue();

                    long count = list.size();
                    long buyIn = list.stream().mapToLong(this::getBuyIn).sum();
                    long prize = list.stream().mapToLong(this::getPrize).sum();
                    long profit = list.stream().mapToLong(this::getProfit).sum();

                    return new MonthlyDailyItem(date, count, buyIn, prize, profit);
                })
                .sorted(Comparator.comparing(MonthlyDailyItem::date))
                .toList();
    }

    private MonthlyHighlights buildHighlights(List<GameSession> sessions) {
        if (sessions.isEmpty()) {
            return new MonthlyHighlights(null, null, null);
        }

        Comparator<GameSession> profitComparator =
                Comparator.comparingLong(this::getProfit);

        Long best = sessions.stream()
                .max(profitComparator)
                .map(this::getProfit)
                .orElse(null);

        Long worst = sessions.stream()
                .min(profitComparator)
                .map(this::getProfit)
                .orElse(null);

        Integer maxConsecutiveItm = calcMaxConsecutiveItm(sessions);

        return new MonthlyHighlights(best, worst, maxConsecutiveItm);
    }

    private Integer calcMaxConsecutiveItm(List<GameSession> sessions) {
        if (sessions.isEmpty()) {
            return null;
        }

        // 날짜 + id 기준 정렬
        List<GameSession> sorted = sessions.stream()
                .sorted(Comparator
                        .comparing(GameSession::getPlayDate)
                        .thenComparing(GameSession::getId))
                .toList();

        int maxStreak = 0;
        int currentStreak = 0;

        for (GameSession s : sorted) {
            if (getPrize(s) > 0L) {
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
            } else {
                currentStreak = 0;
            }
        }

        return maxStreak;
    }

    // ---- 안전한 값 읽기 헬퍼들 ----

    private long getBuyIn(GameSession s) {
        Long v = s.getTotalBuyIn();
        return v != null ? v : 0L;
    }

    private long getPrize(GameSession s) {
        Long v = s.getPrize();
        return v != null ? v : 0L;
    }

    private long getProfit(GameSession s) {
        Long v = s.getNetProfit();
        return v != null ? v : 0L;
    }
}
