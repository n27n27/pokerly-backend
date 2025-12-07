package com.rolling.pokerly.statistics.application;

import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.journal.domain.GameJournal;
import com.rolling.pokerly.journal.repo.GameJournalRepository;
import com.rolling.pokerly.statistics.dto.StatisticsSessionResponse;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.venue.repo.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsSessionService {

    private final GameSessionRepository sessionRepository;
    private final GameJournalRepository journalRepository;
    private final VenueRepository venueRepository;

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

        // 2) 타입별 성과
        var byType = list.stream()
                .collect(Collectors.groupingBy(GameSession::getSessionType))
                .entrySet()
                .stream()
                .map(e -> buildTypeStat(e.getKey(), e.getValue()))
                .toList();

        // 3) 매장별 손익 (Top3만) - venueId -> venueName을 한 번에 조회
        Map<Long, List<GameSession>> byVenueMap = list.stream()
                .collect(Collectors.groupingBy(GameSession::getVenueId));

        // null 아닌 venueId만 모아서 한 번에 조회
        Set<Long> venueIds = byVenueMap.keySet().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> venueNameMap = venueRepository.findAllById(venueIds).stream()
                .collect(Collectors.toMap(
                        Venue::getId,
                        Venue::getName
                ));

        var byVenue = byVenueMap.entrySet()
                .stream()
                .map(e -> buildVenueStat(e.getKey(), e.getValue(), venueNameMap))
                .sorted(Comparator.comparing(StatisticsSessionResponse.VenueStat::totalProfit)
                        .reversed())
                .limit(3)
                .toList();

        // 4) ITM 패턴
        var itmPattern = buildItmPattern(list);

        // 5) 손익 분포
        var distribution = buildDistribution(list);

        // 6) 컨디션 분석 (일지 + 하루 손익)
        var condition = buildConditionAnalysis(userId, list);

        // 7) Top / Worst 세션
        var top = list.stream()
                .sorted(Comparator.comparing(GameSession::getNetProfit).reversed())
                .limit(3)
                .map(s -> simpleSession(s, venueNameMap))
                .toList();

        var worst = list.stream()
                .sorted(Comparator.comparing(GameSession::getNetProfit))
                .limit(3)
                .map(s -> simpleSession(s, venueNameMap))
                .toList();

        return new StatisticsSessionResponse(
                summary,
                byType,
                byVenue,
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

    /**
     * venueId -> venueName 매핑 버전 (N+1 방지)
     */
    private StatisticsSessionResponse.VenueStat buildVenueStat(
            Long venueId,
            List<GameSession> list,
            Map<Long, String> venueNameMap
    ) {
        String name;
        if (venueId == null) {
            name = "기타";
        } else {
            // 조회 안 되면 "삭제된 매장"으로 표시
            name = venueNameMap.getOrDefault(venueId, "삭제된 매장");
        }

        long sessions = list.size();
        long totalBuyIn = list.stream().mapToLong(s -> safe(s.getTotalBuyIn())).sum();
        long profit = list.stream().mapToLong(s -> safe(s.getNetProfit())).sum();
        long itm = list.stream().filter(s -> safe(s.getPrize()) > 0).count();

        double roi = totalBuyIn == 0 ? 0 : (double) profit / totalBuyIn * 100;
        double itmRatio = sessions == 0 ? 0 : (double) itm / sessions;

        return new StatisticsSessionResponse.VenueStat(
                venueId,
                name,
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

    /**
     * 일지 + 하루 손익/바인을 이용해서
     * 점수별 횟수, 평균 손익, 평균 ROI를 계산한다.
     */
    private StatisticsSessionResponse.ConditionAnalysis buildConditionAnalysis(
            Long userId,
            List<GameSession> sessions
    ) {
        // 날짜별 손익 합계
        Map<LocalDate, Long> profitByDate = sessions.stream()
                .collect(Collectors.groupingBy(
                        GameSession::getPlayDate,
                        Collectors.summingLong(s -> safe(s.getNetProfit()))
                ));

        // 🔥 날짜별 바인 합계 (ROI 계산용)
        Map<LocalDate, Long> buyInByDate = sessions.stream()
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
        // 피로 / 에너지는 일단 energyScore 기준 (원하면 나중에 조합 로직 넣기)
        var byFatigue = aggregateScore(journals, profitByDate, buyInByDate, GameJournal::getEnergyScore);

        return new StatisticsSessionResponse.ConditionAnalysis(
                byCondition,
                byMental,
                byFatigue
        );
    }

    /**
     * 점수별:
     *  - count: 해당 점수 일수
     *  - avgProfit: 하루 평균 손익
     *  - avgRoi: (해당 점수 날들의 profit 합 / buy-in 합) * 100
     */
    private List<StatisticsSessionResponse.ConditionAnalysis.ConditionEntry> aggregateScore(
            List<GameJournal> journals,
            Map<LocalDate, Long> profitByDate,
            Map<LocalDate, Long> buyInByDate,
            Function<GameJournal, Integer> getter
    ) {
        // [0] = count, [1] = profitSum, [2] = buyInSum
        Map<Integer, long[]> acc = new HashMap<>();
        for (GameJournal j : journals) {
            Integer score = getter.apply(j);
            if (score == null) continue;

            long profit = profitByDate.getOrDefault(j.getJournalDate(), 0L);
            long buyIn = buyInByDate.getOrDefault(j.getJournalDate(), 0L);

            long[] v = acc.computeIfAbsent(score, k -> new long[3]);
            v[0]++;          // count
            v[1] += profit;  // profit sum
            v[2] += buyIn;   // buy-in sum
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
                    double avgRoi = (sumBuyIn == 0)
                            ? 0.0
                            : (double) sumProfit / sumBuyIn * 100;

                    return new StatisticsSessionResponse.ConditionAnalysis.ConditionEntry(
                            score,
                            count,
                            avgProfit,
                            avgRoi
                    );
                })
                .toList();
    }

    private StatisticsSessionResponse.SimpleSession simpleSession(
            GameSession s,
            Map<Long, String> venueNameMap
    ) {
        long totalBuyIn = safe(s.getTotalBuyIn());
        long profit = safe(s.getNetProfit());
        double roi = totalBuyIn == 0 ? 0 : (double) profit / totalBuyIn * 100;

        Long venueId = s.getVenueId();
        String venueName;
        if (venueId == null) {
            venueName = "기타";
        } else {
            venueName = venueNameMap.getOrDefault(venueId, "삭제된 매장");
        }

        return new StatisticsSessionResponse.SimpleSession(
                s.getId(),
                s.getPlayDate() != null ? s.getPlayDate().toString() : "날짜 미지정",
                totalBuyIn,
                safe(s.getPrize()),
                profit,
                roi,
                venueName,
                s.getSessionType()
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
                List.of(),
                new StatisticsSessionResponse.ItmPattern(0, 0),
                new StatisticsSessionResponse.ProfitDistribution(List.of(), 0, 0, 0),
                new StatisticsSessionResponse.ConditionAnalysis(List.of(), List.of(), List.of()),
                List.of(),
                List.of()
        );
    }
}
