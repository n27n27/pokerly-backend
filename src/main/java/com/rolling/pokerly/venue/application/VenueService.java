package com.rolling.pokerly.venue.application;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.venue.dto.VenueRequest;
import com.rolling.pokerly.venue.dto.VenueResponse;
import com.rolling.pokerly.venue.repo.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final GameSessionRepository gameSessionRepository;

    public List<VenueResponse> getMyVenues(Long userId) {
        return venueRepository.findByCreatedByUserIdOrderByNameAsc(userId).stream()
                .map(VenueResponse::from)
                .toList();
    }

    public VenueResponse getMyVenue(Long userId, Long venueId) {
        var venue = venueRepository.findByIdAndCreatedByUserId(venueId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "매장을 찾을 수 없습니다."));
        return VenueResponse.from(venue);
    }

    @Transactional
    public VenueResponse create(Long userId, VenueRequest req) {

        if (venueRepository.existsByCreatedByUserIdAndName(userId, req.name())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "VENUE_ALREADY_EXISTS.");
        }

        long safePoint = Objects.requireNonNullElse(req.pointBalance(), 0L);

        var venue = Venue.builder()
                .createdByUserId(userId)
                .name(req.name())
                .location(req.location())
                .notes(req.notes())
                .pointBalance(safePoint)
                .build();

        var saved = venueRepository.save(venue);
        return VenueResponse.from(saved);
    }

    @Transactional
    public VenueResponse update(Long userId, Long venueId, VenueRequest req) {
        var venue = venueRepository.findByIdAndCreatedByUserId(venueId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "매장을 찾을 수 없습니다."));

        if (!venue.getName().equals(req.name())
                && venueRepository.existsByCreatedByUserIdAndName(userId, req.name())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "VENUE_ALREADY_EXISTS.");
        }

        long safePoint = Objects.requireNonNullElse(req.pointBalance(), 0L);

        venue.update(req.name(), req.location(), req.notes(), safePoint);
        return VenueResponse.from(venue);
    }

    @Transactional
    public void delete(Long userId, Long venueId) {
        var venue = venueRepository.findByIdAndCreatedByUserId(venueId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "매장을 찾을 수 없습니다."));

        // 해당 매장에 연결된 게임 세션 존재 여부만 체크
        boolean hasGameSessions = gameSessionRepository.existsByUserIdAndVenueId(userId, venueId);

        if (hasGameSessions) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "VENUE_HAS_HISTORY",
                    "해당 매장에 등록된 게임 이력이 있어 삭제할 수 없습니다."
            );
        }

        venueRepository.delete(venue);
    }
}
