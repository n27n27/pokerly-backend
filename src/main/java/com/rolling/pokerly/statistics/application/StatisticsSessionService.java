package com.rolling.pokerly.statistics.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.journal.domain.GameJournal;
import com.rolling.pokerly.journal.repo.GameJournalRepository;
import com.rolling.pokerly.statistics.dto.StatisticsSessionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsSessionService {

    private final GameSessionRepository sessionRepository;
    private final GameJournalRepository journalRepository;

    public StatisticsSessionResponse getSessionStats(Long userId) {

        List<GameSession> list = sessionRepository.findByUserId(userId);
        if (list.isEmpty()) {
            return emptyResponse();
        }

        // 1) Summary
        long totalSessions = list.size();
        long totalBuyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long totalPrize = list.stream().mapToLong(s -> safe(s.getPrize())).sum();
        long totalProfit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        double roi = totalBuyIn == 0 ? 0 : (double) totalProfit / totalBuyIn * 100;

        long itmCount = list.stream().filter(s -> safe(s.getPrize()) > 0).count();
        double itmRatio = totalSessions == 0 ? 0 : (double) itmCount / totalSessions;

        var summary = new StatisticsSessionResponse.Summary(
                totalSessions,
                totalBuyIn,
                totalPrize,
                totalProfit,
                roi,
                itmCount,
                itmRatio
        );

        // 2) 타입별 성과 (✅ sessionType null-safe)
        var byType = list.stream()
                .collect(Collectors.groupingBy(s -> {
                    String t = s.getSessionType();
                    return (t == null || t.isBlank()) ? "UNKNOWN" : t;
                }))
                .entrySet()
                .stream()
                .map(e -> buildTypeStat(e.getKey(), e.getValue()))
                .toList();

        // 3) ITM 패턴 (✅ playDate null-safe sort)
        var itmPattern = buildItmPattern(list);

        // 4) 손익 분포
        var distribution = buildDistribution(list);

        // 5) 컨디션 분석 (일지 + 하루 손익)
        var condition = buildConditionAnalysis(userId, list);

        // 6) Top / Worst 세션 (venueName은 그대로 "기타"만 사용)
        var top = list.stream()
                .sorted(Comparator.comparing(
                        GameSession::getNetProfit,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .limit(3)
                .map(this::simpleSession)
                .toList();

        var worst = list.stream()
                .sorted(Comparator.comparing(
                        GameSession::getNetProfit,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .limit(3)
                .map(this::simpleSession)
                .toList();

        // ✅ byVenue 제거된 생성자 형태에 맞춰 반환
        return new StatisticsSessionResponse(
                summary,
                byType,
                itmPattern,
                distribution,
                condition,
                top,
                worst
        );
    }

    // ===================== Helper builders =====================

    private StatisticsSessionResponse.TypeStat buildTypeStat(String type, List<GameSession> list) {
        long sessions = list.size();
        long totalBuyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long profit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        long itm = list.stream().filter(s -> safe(s.getPrize()) > 0).count();

        double roi = totalBuyIn == 0 ? 0 : (double) profit / totalBuyIn * 100;
        double itmRatio = sessions == 0 ? 0 : (double) itm / sessions;

        return new StatisticsSessionResponse.TypeStat(
                type,
                sessions,
                totalBuyIn,
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
                .sorted(Comparator.comparing(
                        GameSession::getPlayDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
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

    /**
     * 일지 + 하루 손익/바인을 이용해서
     * 점수별 횟수, 평균 손익, 평균 ROI를 계산한다.
     */
    private StatisticsSessionResponse.ConditionAnalysis buildConditionAnalysis(
            Long userId,
            List<GameSession> sessions
    ) {
        var sessionsWithDate = sessions.stream()
                .filter(s -> s.getPlayDate() != null)
                .toList();

        Map<LocalDate, Long> profitByDate = sessionsWithDate.stream()
                .collect(Collectors.groupingBy(
                        GameSession::getPlayDate,
                        Collectors.summingLong(s -> safe(s.getNetProfit()))
                ));

        Map<LocalDate, Long> buyInByDate = sessionsWithDate.stream()
                .collect(Collectors.groupingBy(
                        GameSession::getPlayDate,
                        Collectors.summingLong(s -> safe(s.getTotalBuyIn()))
                ));

        List<GameJournal> journals = journalRepository.findByUserId(userId);
        if (journals.isEmpty()) {
            return new StatisticsSessionResponse.ConditionAnalysis(
                    List.of(), List.of(), List.of()
            );
        }

        var byCondition = aggregateScore(journals, profitByDate, buyInByDate, GameJournal::getMoodScore);
        var byMental = aggregateScore(journals, profitByDate, buyInByDate, GameJournal::getFocusScore);
        var byFatigue = aggregateScore(journals, profitByDate, buyInByDate, GameJournal::getEnergyScore);

        return new StatisticsSessionResponse.ConditionAnalysis(
                byCondition,
                byMental,
                byFatigue
        );
    }

    private List<StatisticsSessionResponse.ConditionAnalysis.ConditionEntry> aggregateScore(
            List<GameJournal> journals,
            Map<LocalDate, Long> profitByDate,
            Map<LocalDate, Long> buyInByDate,
            Function<GameJournal, Integer> getter
    ) {
        Map<Integer, long[]> acc = new HashMap<>(); // [0]=count, [1]=profitSum, [2]=buyInSum

        for (GameJournal j : journals) {
            Integer score = getter.apply(j);
            if (score == null) continue;

            long profit = profitByDate.getOrDefault(j.getJournalDate(), 0L);
            long buyIn = buyInByDate.getOrDefault(j.getJournalDate(), 0L);

            long[] v = acc.computeIfAbsent(score, k -> new long[3]);
            v[0]++;
            v[1] += profit;
            v[2] += buyIn;
        }

        return acc.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    int score = e.getKey();
                    long count = e.getValue()[0];
                    long sumProfit = e.getValue()[1];
                    long sumBuyIn = e.getValue()[2];

                    long avgProfit = count == 0 ? 0 : sumProfit / count;
                    double avgRoi = (sumBuyIn == 0) ? 0.0 : (double) sumProfit / sumBuyIn * 100;

                    return new StatisticsSessionResponse.ConditionAnalysis.ConditionEntry(
                            score,
                            count,
                            avgProfit,
                            avgRoi
                    );
                })
                .toList();
    }

    private StatisticsSessionResponse.SimpleSession simpleSession(GameSession s) {
        long totalBuyIn = safe(s.getTotalBuyIn());
        long profit = safe(s.getNetProfit());
        double roi = totalBuyIn == 0 ? 0 : (double) profit / totalBuyIn * 100;

        // ✅ 세션 통계 탭에서는 매장 Top3를 없앴으니
        // Top/Worst 카드도 venueName을 “기타/매장” 정도만 유지하거나,
        // 프론트에서 필요하면 여기 로직을 다시 확장하면 됨.
        String venueName = (s.getVenueId() == null) ? "기타" : "매장";

        return new StatisticsSessionResponse.SimpleSession(
                s.getId(),
                s.getPlayDate() != null ? s.getPlayDate().toString() : "날짜 미지정",
                totalBuyIn,
                safe(s.getPrize()),
                profit,
                roi,
                venueName,
                s.getSessionType(),
                s.isCollab(),
                s.getCollabLabel()
        );
    }

    // ===================== 기타 =====================

    private long safe(Long v) {
        return v == null ? 0 : v;
    }

    private StatisticsSessionResponse emptyResponse() {
        return new StatisticsSessionResponse(
                new StatisticsSessionResponse.Summary(0, 0, 0, 0, 0, 0, 0),
                List.of(),
                new StatisticsSessionResponse.ItmPattern(0, 0),
                new StatisticsSessionResponse.ProfitDistribution(List.of(), 0, 0, 0),
                new StatisticsSessionResponse.ConditionAnalysis(List.of(), List.of(), List.of()),
                List.of(),
                List.of()
        );
    }
}
