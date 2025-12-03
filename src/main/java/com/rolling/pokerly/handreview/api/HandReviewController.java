package com.rolling.pokerly.handreview.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.handreview.application.HandReviewStoreService;
import com.rolling.pokerly.handreview.dto.HandReviewResponse;
import com.rolling.pokerly.handreview.dto.SimpleAnalyzeRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hand-review")
@RequiredArgsConstructor
public class HandReviewController {

    private final HandReviewStoreService storeService;

    @PostMapping("/simple")
    public ApiResponse<HandReviewResponse> analyzeAndSave(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody SimpleAnalyzeRequest request
    ) {
        var res = storeService.analyzeAndCreate(userId, request);
        return ApiResponse.ok(res);
    }

    @GetMapping
    public ApiResponse<List<HandReviewResponse>> getMyHands(@AuthenticationPrincipal(expression = "userId") Long userId) {

    var res = storeService.getMyHands(userId);
        return ApiResponse.ok(res);
    }

    @GetMapping("/{id}")
    public ApiResponse<HandReviewResponse> getHandDetail(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long id
    ) {
        var res = storeService.getMyHand(userId, id);
        return ApiResponse.ok(res);
    }
}
