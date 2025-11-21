package com.rolling.pokerly.gamesession.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.gamesession.domain.GameSession;
import com.rolling.pokerly.gamesession.dto.GameSessionRequest;
import com.rolling.pokerly.gamesession.dto.GameSessionResponse;
import com.rolling.pokerly.gamesession.repo.GameSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;

    @Transactional
    public GameSessionResponse create(Long userId, GameSessionRequest req) {

        var session = GameSession.builder()
                .userId(userId)
                .venueId(req.getVenueId())
                .playDate(req.getPlayDate())
                .title(req.getTitle())
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
        session.setTitle(req.getTitle());
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

}
