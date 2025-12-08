package com.rolling.pokerly.statistics.application;

import com.rolling.pokerly.statistics.dto.VenueStatsResponse;
import com.rolling.pokerly.statistics.dto.VenueStatsResponse.SummarySection;
import com.rolling.pokerly.statistics.dto.VenueStatsResponse.TopVenueSection;
import com.rolling.pokerly.statistics.dto.VenueStatsResponse.VenueRank;
import com.rolling.pokerly.statistics.dto.VenueStatsResponse.VenueStat;
import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.venue.repo.VenueRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VenueStatsService {

    private final GameSessionRepository gameSessionRepository;
    private final VenueRepository venueRepository;

    public VenueStatsService(
            GameSessionRepository gameSessionRepository,
            VenueRepository venueRepository
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.venueRepository = venueRepository;
    }

    public VenueStatsResponse getVenueStats(Long userId) {
        // 1) 해당 유저의 "매장(VENUE) 세션"만 가져오기 (venueId NOT NULL)
        List<GameSession> sessions = gameSessionRepository.findVenueSessionsByUserId(userId);

        if (sessions.isEmpty()) {
            SummarySection emptySummary = new SummarySection(
                    0, 0L, 0L, 0L, 0.0, 0
            );
            return new VenueStatsResponse(emptySummary, List.of(), new TopVenueSection(null, null, null));
        }

        // 2) 사용된 venueId 전체 모아서 한 번에 매장 정보 로딩
        Set<Long> venueIds = sessions.stream()
                .map(GameSession::getVenueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Venue> venueMap = venueRepository.findAllById(venueIds).stream()
                .collect(Collectors.toMap(Venue::getId, v -> v));

        // 3) venueId 기준으로 세션 그룹핑
        Map<Long, List<GameSession>> byVenueId = sessions.stream()
                .filter(s -> s.getVenueId() != null)
                .collect(Collectors.groupingBy(GameSession::getVenueId));

        // 4) VenueStat 리스트 생성 (profit 기준 내림차순 정렬)
        List<VenueStat> venueStats = byVenueId.entrySet().stream()
                .map(entry -> {
                    Long venueId = entry.getKey();
                    List<GameSession> venueSessions = entry.getValue();
                    Venue venue = venueMap.get(venueId);

                    String venueName = (venue != null) ? venue.getName() : "알 수 없는 매장";

                    return toVenueStat(venueId, venueName, venueSessions);
                })
                .sorted(Comparator.comparing(VenueStat::totalProfit).reversed())
                .collect(Collectors.toList());

        // 5) 전체 Summary 계산
        SummarySection summary = buildSummary(venueStats);

        // 6) Top / Worst 매장 계산
        TopVenueSection topVenueSection = buildTopVenueSection(venueStats);

        return new VenueStatsResponse(summary, venueStats, topVenueSection);
    }

    private VenueStat toVenueStat(Long venueId, String venueName, List<GameSession> sessions) {
        int sessionCount = sessions.size();

        long totalBuyIn = sessions.stream()
                .mapToLong(s -> Optional.ofNullable(s.getTotalBuyIn()).orElse(0L))
                .sum();

        long totalPrize = sessions.stream()
                .mapToLong(s -> Optional.ofNullable(s.getPrize()).orElse(0L))
                .sum();

        // netProfit 필드가 이미 있으므로 써도 되고, 안전하게 다시 계산해도 됨
        long totalProfit = sessions.stream()
                .mapToLong(s -> Optional.ofNullable(s.getNetProfit()).orElse(0L))
                .sum();

        int itmCount = (int) sessions.stream()
                .filter(s -> Optional.ofNullable(s.getPrize()).orElse(0L) > 0L)
                .count();

        double itmRatio = sessionCount == 0
                ? 0.0
                : (double) itmCount / sessionCount;

        double roi = totalBuyIn <= 0
                ? 0.0
                : (double) totalProfit * 100.0 / totalBuyIn;

        // 🔹 평균 엔트리(fieldEntries) 계산:
        // "토너 전체 엔트리 수"가 기록된 세션만 대상으로 함.
        // 예: 세션 5개 중 fieldEntries가 3개만 있으면 분모는 3
        List<Integer> entries = sessions.stream()
                .map(GameSession::getFieldEntries)   // Integer, nullable
                .filter(Objects::nonNull)
                .filter(e -> e > 0)
                .collect(Collectors.toList());

        int entrySampleCount = entries.size();

        Integer avgEntry = null;
        if (entrySampleCount > 0) {
            double avg = entries.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            avgEntry = (int) Math.round(avg);
        }

        return new VenueStat(
                venueId,
                venueName,
                sessionCount,
                totalBuyIn,
                totalPrize,
                totalProfit,
                roi,
                itmCount,
                itmRatio,
                avgEntry,
                entrySampleCount
        );
    }

    private SummarySection buildSummary(List<VenueStat> venueStats) {
        int totalSessions = venueStats.stream()
                .mapToInt(VenueStat::sessions)
                .sum();

        long totalBuyIn = venueStats.stream()
                .mapToLong(VenueStat::totalBuyIn)
                .sum();

        long totalPrize = venueStats.stream()
                .mapToLong(VenueStat::totalPrize)
                .sum();

        long totalProfit = venueStats.stream()
                .mapToLong(VenueStat::totalProfit)
                .sum();

        double roi = totalBuyIn <= 0
                ? 0.0
                : (double) totalProfit * 100.0 / totalBuyIn;

        int totalVenues = venueStats.size();

        return new SummarySection(
                totalSessions,
                totalBuyIn,
                totalPrize,
                totalProfit,
                roi,
                totalVenues
        );
    }

    private TopVenueSection buildTopVenueSection(List<VenueStat> venueStats) {
        // 누적 수익 기준 최고 / 최저
        VenueRank bestByProfit = venueStats.stream()
                .max(Comparator.comparingLong(VenueStat::totalProfit))
                .map(this::toVenueRank)
                .orElse(null);

        VenueRank worstByProfit = venueStats.stream()
                .min(Comparator.comparingLong(VenueStat::totalProfit))
                .map(this::toVenueRank)
                .orElse(null);

        // ROI 기준 최고 (분모>0 인 것만)
        VenueRank bestByRoi = venueStats.stream()
                .filter(v -> v.totalBuyIn() > 0)
                .max(Comparator.comparingDouble(VenueStat::roi))
                .map(this::toVenueRank)
                .orElse(null);

        return new TopVenueSection(bestByProfit, worstByProfit, bestByRoi);
    }

    private VenueRank toVenueRank(VenueStat v) {
        return new VenueRank(
                v.venueId(),
                v.venueName(),
                v.totalProfit(),
                v.roi()
        );
    }
}
