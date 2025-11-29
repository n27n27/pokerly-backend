package com.rolling.pokerly.gamesession.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.controller.BaseController;
import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.gamesession.application.GameSessionService;
import com.rolling.pokerly.gamesession.dto.GameSessionOptionResponse;
import com.rolling.pokerly.gamesession.dto.GameSessionRequest;
import com.rolling.pokerly.gamesession.dto.GameSessionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/game-sessions")
@RequiredArgsConstructor
public class GameSessionController extends BaseController {

    private final GameSessionService gameSessionService;

    @GetMapping
    public ApiResponse<List<GameSessionResponse>> getMonthlySessions(
            Authentication auth,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(name = "venueId", required = false) Long venueId
    ) {
        Long userId = getUserId(auth);
        var list = gameSessionService.getMonthlySessions(userId, year, month);
        return ApiResponse.ok(list);
    }

    @GetMapping("/{id}")
    public ApiResponse<GameSessionResponse> getOne(
            Authentication auth,
            @PathVariable("id") Long id
    ) {
        Long userId = getUserId(auth);
        var res = gameSessionService.getOne(userId, id);
        return ApiResponse.ok(res);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GameSessionResponse> create(
            Authentication auth,
            @RequestBody GameSessionRequest req
    ) {
        Long userId = getUserId(auth);
        var created = gameSessionService.create(userId, req);
        return ApiResponse.ok(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<GameSessionResponse> update(
            Authentication auth,
            @PathVariable("id") Long id,
            @RequestBody GameSessionRequest req
    ) {
        Long userId = getUserId(auth);
        var updated = gameSessionService.update(userId, id, req);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            Authentication auth,
            @PathVariable("id") Long id
    ) {
        Long userId = getUserId(auth);
        gameSessionService.delete(userId, id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/options")
    public ApiResponse<List<GameSessionOptionResponse>> getSessionOptions(Authentication auth) {
        Long userId = getUserId(auth);
        var res = gameSessionService.getSessionOptions(userId);
        return ApiResponse.ok(res);
    }
}