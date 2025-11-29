package com.rolling.pokerly.venue.api;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.controller.BaseController;
import com.rolling.pokerly.core.response.ApiResponse;
import com.rolling.pokerly.venue.application.VenueService;
import com.rolling.pokerly.venue.dto.VenueRequest;
import com.rolling.pokerly.venue.dto.VenueResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController extends BaseController {

    private final VenueService venueService;

    @GetMapping
    public ApiResponse<List<VenueResponse>> getMyVenues(Authentication auth) {

        Long userId = getUserId(auth);

        var venues = venueService.getMyVenues(userId);
        return ApiResponse.ok(venues);
    }

    @PostMapping
    public ApiResponse<VenueResponse> createVenue(Authentication auth,
            @RequestBody VenueRequest req) {

        Long userId = getUserId(auth);
        var created = venueService.create(userId, req);
        return ApiResponse.ok(created);
    }

    @PutMapping("/{venueId}")
    public ApiResponse<VenueResponse> updateVenue(Authentication auth,
            @PathVariable("venueId") Long venueId,
            @RequestBody VenueRequest req) {

        Long userId = getUserId(auth);
        var updated = venueService.update(userId, venueId, req);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{venueId}")
    public ApiResponse<Void> deleteVenue(Authentication auth, @PathVariable("venueId") Long venueId) {

        Long userId = getUserId(auth);
        venueService.delete(userId, venueId);
        return ApiResponse.ok(null);
    }

}


