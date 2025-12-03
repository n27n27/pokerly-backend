package com.rolling.pokerly.dashboard.api;

import java.time.YearMonth;
import java.time.ZoneId;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.dashboard.application.DashboardService;
import com.rolling.pokerly.dashboard.dto.DashboardMonthlyResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 대시보드 API
 */
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 월간 대시보드
     *
     * 예)
     *  GET /api/dashboard/monthly?year=2025&month=12
     * year, month 생략 시 현재 월 기준
     */
    @GetMapping("/monthly")
    public ApiResponse<DashboardMonthlyResponse> getMonthly(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month
    ) {
        var nowYm = YearMonth.now(ZoneId.of("Asia/Seoul"));
        log.info("대시보드 월간 조회 요청 userId={}, year={}, month={}", userId, year, month);

        int y = (year != null) ? year : nowYm.getYear();
        int m = (month != null) ? month : nowYm.getMonthValue();

        var res = dashboardService.getMonthly(userId, y, m);
        return ApiResponse.ok(res);
    }
}
