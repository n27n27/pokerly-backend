package com.rolling.pokerly.gamesession.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.dto.GameSessionOptionResponse;
import com.rolling.pokerly.gamesession.dto.GameSessionRequest;
import com.rolling.pokerly.gamesession.dto.GameSessionResponse;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;
import com.rolling.pokerly.venue.domain.Venue;
import com.rolling.pokerly.venue.repo.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public GameSessionResponse create(Long userId, GameSessionRequest req) {

        var session = GameSession.builder()
                .userId(userId)
                .venueId(req.getVenueId())
                .playDate(req.getPlayDate())
                .gameType(req.getGameType())
                .buyInPerEntry(req.getBuyInPerEntry())
                .entries(req.getEntries())
                .discount(req.getDiscount())
                .prize(req.getPrize())
                .notes(req.getNotes())
                .build();
        session.recalc();
        gameSessionRepository.save(session);

        return GameSessionResponse.from(session);
    }

    @Transactional
    public GameSessionResponse update(Long userId, Long sessionId, GameSessionRequest req) {
        var session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));

        session.setVenueId(req.getVenueId());
        session.setPlayDate(req.getPlayDate());
        session.setGameType(req.getGameType());
        session.setBuyInPerEntry(req.getBuyInPerEntry());
        session.setEntries(req.getEntries());
        session.setDiscount(req.getDiscount());
        session.setPrize(req.getPrize());
        session.setNotes(req.getNotes());

        session.recalc();

        return GameSessionResponse.from(session);
    }

    @Transactional
    public void delete(Long userId, Long sessionId) {

        var session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));

        gameSessionRepository.delete(session);
    }

    public GameSessionResponse getOne(Long userId, Long sessionId) {
        var session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));
        return GameSessionResponse.from(session);
    }

    public List<GameSessionResponse> getMonthlySessions(Long userId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return gameSessionRepository
                .findByUserIdAndPlayDateBetweenOrderByPlayDateAsc(userId, start, end)
                .stream()
                .map(GameSessionResponse::from)
                .toList();
    }

    public List<GameSessionOptionResponse> getSessionOptions(Long userId) {

        var sessions = gameSessionRepository.findTop100ByUserIdOrderByPlayDateDescIdDesc(userId);

        List<GameSessionOptionResponse> result = new ArrayList<>();

        for (var s : sessions) {
            var venueName = resolveVenueName(s.getVenueId());
            var label = buildLabel(s, venueName);

            result.add(new GameSessionOptionResponse(
                    s.getId(),
                    label
            ));
        }

        return result;
    }

    private String resolveVenueName(Long venueId) {
        if (venueId == null) return null;
        return venueRepository.findById(venueId)
                .map(Venue::getName)
                .orElse(null);
    }

    private String buildLabel(GameSession s, String venueName) {

        var parts = new ArrayList<String>();

        // 1) 날짜
        if (s.getPlayDate() != null) {
            parts.add(s.getPlayDate().toString());
        }

        // 2) 매장 이름
        if (venueName != null && !venueName.isBlank()) {
            parts.add(venueName);
        }

       return String.join(" · ", parts);
    }


}
