package com.rolling.pokerly.point.api;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.controller.BaseController;
import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.point.application.PointService;
import com.rolling.pokerly.point.dto.PointAdjustRequest;
import com.rolling.pokerly.point.dto.PointBalanceResponse;
import com.rolling.pokerly.point.dto.PointEarnRequest;
import com.rolling.pokerly.point.dto.PointTransactionResponse;
import com.rolling.pokerly.point.dto.PointUseRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController extends BaseController{

    private final PointService pointService;

    @GetMapping("/venues/{venueId}/balance")
    public ApiResponse<PointBalanceResponse> getBalance(
            Authentication auth,
            @PathVariable("venueId") Long venueId
    ) {
        Long userId = getUserId(auth);
        var res = pointService.getBalance(userId, venueId);
        return ApiResponse.ok(res);
    }

    @GetMapping("/venues/{venueId}/transactions")
    public ApiResponse<List<PointTransactionResponse>> getTransactions(
            Authentication auth,
            @PathVariable("venueId") Long venueId,
            @RequestParam(name = "limit", required = false) Long limit
    ) {
        Long userId = getUserId(auth);
        var l = Objects.requireNonNullElse(limit, 50L);
        var res = pointService.getTransactions(userId, venueId, l);
        return ApiResponse.ok(res);
    }

    @PostMapping("/earn")
    public ApiResponse<PointTransactionResponse> earn(
            Authentication auth,
            @RequestBody PointEarnRequest req
    ) {
        Long userId = getUserId(auth);
        var res = pointService.earn(userId, req);
        return ApiResponse.ok(res);
    }

    @PostMapping("/use")
    public ApiResponse<PointTransactionResponse> use(
            Authentication auth,
            @RequestBody PointUseRequest req
    ) {
        Long userId = getUserId(auth);
        var res = pointService.use(userId, req);
        return ApiResponse.ok(res);
    }

    @PostMapping("/adjust")
    public ApiResponse<PointTransactionResponse> adjust(
            Authentication auth,
            @RequestBody PointAdjustRequest req
    ) {
        Long userId = getUserId(auth);
        var res = pointService.adjust(userId, req);
        return ApiResponse.ok(res);
    }

}
