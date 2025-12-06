package com.rolling.pokerly.statistics.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.statistics.application.MonthlyStatisticsService;
import com.rolling.pokerly.statistics.dto.MonthlyStatisticsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final MonthlyStatisticsService monthlyStatisticsService;

    @GetMapping("/monthly")
    public MonthlyStatisticsResponse getMonthlyStatistics(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {

        return monthlyStatisticsService.getMonthlyStatistics(userId, year, month);
    }
}
