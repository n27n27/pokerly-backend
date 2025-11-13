package com.rolling.pokerly.gamesession.api;

import java.time.YearMonth;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.gamesession.application.GameSessionService;
import com.rolling.pokerly.gamesession.dto.GameSessionRequest;
import com.rolling.pokerly.gamesession.dto.GameSessionResponse;
import com.rolling.pokerly.gamesession.dto.MonthSummaryResponse;
import com.rolling.pokerly.security.jwt.CustomPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/game-sessions")
@RequiredArgsConstructor
@Slf4j
public class GameSessionController {

    private final GameSessionService service;

    private Long userId(Authentication auth) {
        CustomPrincipal p = (CustomPrincipal) auth.getPrincipal();
        return p.getUserId();
    }

    @PostMapping
    public ApiResponse<GameSessionResponse> create(Authentication auth, @RequestBody GameSessionRequest req) {
        var result = service.create(userId(auth), req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<GameSessionResponse> update(Authentication auth, @PathVariable("id") Long id, @RequestBody GameSessionRequest req) {
        var result = service.update(userId(auth), id, req);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(Authentication auth, @PathVariable("id") Long id) {
        service.delete(userId(auth), id);
        return ApiResponse.ok("delete");
    }

    @GetMapping("/{id}")
    public ApiResponse<GameSessionResponse> get(Authentication auth, @PathVariable("id") Long id) {
        var result = service.get(userId(auth), id);
        return ApiResponse.ok(result);
    }

    @GetMapping
    public ApiResponse<List<GameSessionResponse>> list(Authentication auth,
                                          @RequestParam("year") int year,
                                          @RequestParam("month") int month,
                                          @RequestParam(name = "venueId", required = false) Long venueId) {
        var result = service.listByMonth(userId(auth), YearMonth.of(year, month), venueId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/summary")
    public ApiResponse<MonthSummaryResponse> summary(Authentication auth,
                                        @RequestParam("year") int year,
                                        @RequestParam("month") int month,
                                        @RequestParam(name = "venueId", required = false) Long venueId) {
        var result = service.summaryByMonth(userId(auth), YearMonth.of(year, month), venueId);
        return ApiResponse.ok(result);
    }
}
