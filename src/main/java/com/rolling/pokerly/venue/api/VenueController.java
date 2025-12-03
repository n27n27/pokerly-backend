package com.rolling.pokerly.venue.api;

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
import com.rolling.pokerly.venue.application.VenueService;
import com.rolling.pokerly.venue.dto.VenueRequest;
import com.rolling.pokerly.venue.dto.VenueResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ApiResponse<List<VenueResponse>> getMyVenues(@AuthenticationPrincipal(expression = "userId") Long userId) {

        var venues = venueService.getMyVenues(userId);
        return ApiResponse.ok(venues);
    }

    // ✅ 단일 매장 조회 추가
    @GetMapping("/{venueId}")
    public ApiResponse<VenueResponse> getVenue(@AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("venueId") Long venueId) {

        var venue = venueService.getMyVenue(userId, venueId);
        return ApiResponse.ok(venue);
    }

    @PostMapping
    public ApiResponse<VenueResponse> createVenue(@AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody VenueRequest req) {

        var created = venueService.create(userId, req);
        return ApiResponse.ok(created);
    }

    @PutMapping("/{venueId}")
    public ApiResponse<VenueResponse> updateVenue(@AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("venueId") Long venueId,
            @RequestBody VenueRequest req) {

        var updated = venueService.update(userId, venueId, req);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{venueId}")
    public ApiResponse<Void> deleteVenue(@AuthenticationPrincipal(expression = "userId") Long userId, @PathVariable("venueId") Long venueId) {

        venueService.delete(userId, venueId);
        return ApiResponse.ok(null);
    }
}
