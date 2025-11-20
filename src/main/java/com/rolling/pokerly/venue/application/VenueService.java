package com.rolling.pokerly.venue.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.point.repo.PointTransactionRepository;
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
    private final PointTransactionRepository pointTransactionRepository;

    public List<VenueResponse> getMyVenues(Long userId) {
        return venueRepository.findByCreatedByUserIdOrderByNameAsc(userId).stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional
    public VenueResponse create(Long userId, VenueRequest req) {

        if (venueRepository.existsByCreatedByUserIdAndName(userId, req.getName())) {

            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "VENUE_ALREADY_EXISTS.");
        }

        var venue = Venue.builder()
                .createdByUserId(userId)
                .name(req.getName())
                .location(req.getLocation())
                .notes(req.getNotes())
                .build();

        var saved = venueRepository.save(venue);
        return VenueResponse.from(saved);
    }

    @Transactional
    public VenueResponse update(Long userId, Long venueId, VenueRequest req) {
        var venue = venueRepository.findByIdAndCreatedByUserId(venueId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "매장을 찾을 수 없습니다."));

        if (!venue.getName().equals(req.getName())
                && venueRepository.existsByCreatedByUserIdAndName(userId, req.getName())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "VENUE_ALREADY_EXISTS.");
        }

        venue.update(req.getName(), req.getLocation(), req.getNotes());
        return VenueResponse.from(venue);
    }

    @Transactional
    public void delete(Long userId, Long venueId) {
        var venue = venueRepository.findByIdAndCreatedByUserId(venueId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "매장을 찾을 수 없습니다."));

        // 1) 해당 매장에 연결된 게임 세션 존재 여부
        boolean hasGameSessions = gameSessionRepository.existsByUserIdAndVenueId(userId, venueId);

        // 2) 해당 매장에 연결된 포인트 트랜잭션 존재 여부
        boolean hasPointTransactions = pointTransactionRepository.existsByUserIdAndVenueId(userId, venueId);

        if (hasGameSessions || hasPointTransactions) {
            // 이미 정의해둔 전역 예외 형태에 맞춰서 코드/메시지 설정
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "VENUE_HAS_HISTORY",
                    "해당 매장에 등록된 게임/포인트 이력이 있어 삭제할 수 없습니다."
            );
        }

        venueRepository.delete(venue);
    }
}
