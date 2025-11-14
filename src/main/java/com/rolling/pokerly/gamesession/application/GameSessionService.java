package com.rolling.pokerly.gamesession.application;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

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

    public List<GameSessionResponse> getMonthlySessions(Long userId, int year, int month, Long venueId) {
        var ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        var sessions = (venueId == null)
            ? gameSessionRepository.findByUserIdAndPlayDateBetweenOrderByPlayDateAsc(userId, from, to)
            : gameSessionRepository.findByUserIdAndVenueIdAndPlayDateBetweenOrderByPlayDateAsc(
                    userId, venueId, from, to);
        return sessions.stream()
                .map(GameSessionResponse::from)
                .toList();
    }

    public GameSessionResponse getOne(Long userId, Long sessionId) {
        var s = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));
        return GameSessionResponse.from(s);
    }

    @Transactional
    public GameSessionResponse create(Long userId, GameSessionRequest req) {
        // 기본값 방어
        var entries        = Objects.requireNonNullElse(req.getEntries(), 1);
        var totalCashIn    = Objects.requireNonNullElse(req.getTotalCashIn(), 0L);
        var totalPointIn   = Objects.requireNonNullElse(req.getTotalPointIn(), 0L);
        var cashOut        = Objects.requireNonNullElse(req.getCashOut(), 0L);
        var discount       = Objects.requireNonNullElse(req.getDiscount(), 0L);

        var session = GameSession.builder()
                .userId(userId)
                .venueId(req.getVenueId())
                .playDate(req.getPlayDate())
                .title(req.getTitle())
                .gameType(req.getGameType())
                .totalCashIn(totalCashIn)
                .totalPointIn(totalPointIn)
                .entries(entries)
                .cashOut(cashOut)
                .discount(discount)
                .notes(req.getNotes())
                // profit / createdAt / updatedAt 은 @PrePersist에서 계산
                .build();

        var saved = gameSessionRepository.save(session);
        return GameSessionResponse.from(saved);
    }

    @Transactional
    public GameSessionResponse update(Long userId, Long sessionId, GameSessionRequest req) {
        var s = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));

        var entries        = Objects.requireNonNullElse(req.getEntries(), 1);
        var totalCashIn    = Objects.requireNonNullElse(req.getTotalCashIn(), 0L);
        var totalPointIn   = Objects.requireNonNullElse(req.getTotalPointIn(), 0L);
        var cashOut        = Objects.requireNonNullElse(req.getCashOut(), 0L);
        var discount       = Objects.requireNonNullElse(req.getDiscount(), 0L);

        s.update(
                req.getPlayDate(),
                req.getVenueId(),
                req.getTitle(),
                req.getGameType(),
                totalCashIn,
                totalPointIn,
                entries,
                cashOut,
                discount,
                req.getNotes()
        );
        // @PreUpdate 에서 profit/updatedAt 자동 계산

        return GameSessionResponse.from(s);
    }

    @Transactional
    public void delete(Long userId, Long sessionId) {
        var s = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));

        gameSessionRepository.delete(s);
    }

}
