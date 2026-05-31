package com.rolling.pokerly.handlog.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.handlog.application.HandLogService;
import com.rolling.pokerly.handlog.dto.HandLogBlindLevelCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogBlindLevelResponse;
import com.rolling.pokerly.handlog.dto.HandLogEventCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogEventResponse;
import com.rolling.pokerly.handlog.dto.HandLogHandCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogHandResponse;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/hand-log")
@RequiredArgsConstructor
public class HandLogController {

    private final HandLogService handLogService;

    @GetMapping("/events")
    public ApiResponse<List<HandLogEventResponse>> getMyEvents(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        var res = handLogService.getMyEvents(userId);
        return ApiResponse.ok(res);
    }

    @PostMapping("/events")
    public ApiResponse<HandLogEventResponse> createEvent(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody HandLogEventCreateRequest request
    ) {
        var res = handLogService.createEvent(userId, request);
        return ApiResponse.ok(res);
    }

    @GetMapping("/events/{eventId}")
    public ApiResponse<HandLogEventResponse> getEventDetail(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId
    ) {
        var res = handLogService.getEventDetail(userId, eventId);
        return ApiResponse.ok(res);
    }

    @PostMapping("/events/{eventId}/blind-levels")
    public ApiResponse<HandLogBlindLevelResponse> createBlindLevel(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @RequestBody HandLogBlindLevelCreateRequest request
    ) {
        var res = handLogService.createBlindLevel(userId, eventId, request);
        return ApiResponse.ok(res);
    }

    @GetMapping("/events/{eventId}/blind-levels/{blindLevelId}")
    public ApiResponse<HandLogBlindLevelResponse> getBlindLevelDetail(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "blindLevelId") Long blindLevelId
    ) {
        var res = handLogService.getBlindLevelDetail(userId, eventId, blindLevelId);
        return ApiResponse.ok(res);
    }

    @PostMapping("/events/{eventId}/blind-levels/{blindLevelId}/hands")
    public ApiResponse<HandLogHandResponse> createHand(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "blindLevelId") Long blindLevelId,
            @RequestBody HandLogHandCreateRequest request
    ) {
        var res = handLogService.createHand(userId, eventId, blindLevelId, request);
        return ApiResponse.ok(res);
    }

    @GetMapping("/events/{eventId}/blind-levels/{blindLevelId}/hands/{handId}")
    public ApiResponse<HandLogHandResponse> getHandDetail(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "blindLevelId") Long blindLevelId,
            @PathVariable(name = "handId") Long handId
    ) {
        var res = handLogService.getHandDetail(userId, eventId, blindLevelId, handId);
        return ApiResponse.ok(res);
    }

    @PutMapping("/events/{eventId}/blind-levels/{blindLevelId}/hands/{handId}")
    public ApiResponse<HandLogHandResponse> updateHand(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "blindLevelId") Long blindLevelId,
            @PathVariable(name = "handId") Long handId,
            @RequestBody HandLogHandCreateRequest request
    ) {
        var res = handLogService.updateHand(userId, eventId, blindLevelId, handId, request);
        return ApiResponse.ok(res);
    }

    @DeleteMapping("/events/{eventId}/blind-levels/{blindLevelId}/hands/{handId}")
    public ApiResponse<Void> deleteHand(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "blindLevelId") Long blindLevelId,
            @PathVariable(name = "handId") Long handId
    ) {
        handLogService.deleteHand(userId, eventId, blindLevelId, handId);
        return ApiResponse.ok(null);
    }
}