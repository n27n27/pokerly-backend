package com.rolling.pokerly.statistics.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.statistics.application.MonthlyStatisticsService;
import com.rolling.pokerly.statistics.application.StatisticsSessionService;
import com.rolling.pokerly.statistics.application.VenueStatsService;
import com.rolling.pokerly.statistics.dto.MonthlyStatisticsResponse;
import com.rolling.pokerly.statistics.dto.StatisticsSessionResponse;
import com.rolling.pokerly.statistics.dto.VenueStatsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final MonthlyStatisticsService monthlyStatisticsService;
    private final StatisticsSessionService sessionService;
    private final VenueStatsService venueStatsService;

    @GetMapping("/monthly")
    public MonthlyStatisticsResponse getMonthlyStatistics(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {

        return monthlyStatisticsService.getMonthlyStatistics(userId, year, month);
    }

    @GetMapping("/sessions")
    public StatisticsSessionResponse getSessionStats(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return sessionService.getSessionStats(userId);
    }

    @GetMapping("/venues")
    public VenueStatsResponse getVenueStatistics(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return venueStatsService.getVenueStats(userId);
    }
}
