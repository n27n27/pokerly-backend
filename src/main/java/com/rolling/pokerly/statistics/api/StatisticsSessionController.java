package com.rolling.pokerly.statistics.api;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.statistics.application.StatisticsSessionService;
import com.rolling.pokerly.statistics.dto.StatisticsSessionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statistics/sessions")
@RequiredArgsConstructor
public class StatisticsSessionController {

    private final StatisticsSessionService service;

    @GetMapping
    public StatisticsSessionResponse getSessionStats(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return service.getSessionStats(userId);
    }
}
