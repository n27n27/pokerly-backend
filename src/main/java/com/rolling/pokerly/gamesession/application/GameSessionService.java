package com.rolling.pokerly.gamesession.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // 허용 세션 타입
    private static final Set<String> ALLOWED_SESSION_TYPES = Set.of(
            GameSession.SESSION_TYPE_VENUE,
            GameSession.SESSION_TYPE_MAJOR,
            GameSession.SESSION_TYPE_ONLINE,
            GameSession.SESSION_TYPE_OTHER
    );

    // =========================
    // CRUD
    // =========================

    @Transactional
    public GameSessionResponse create(Long userId, GameSessionRequest req) {

        String sessionType = normalizeSessionType(req.sessionType());

        VenuePolicy policy = normalizeVenuePolicy(
                sessionType,
                req.venueId(),
                req.isCollab(),
                req.collabLabel()
        );

        var session = GameSession.builder()
                .userId(userId)
                .venueId(policy.venueId())
                .collab(policy.isCollab())
                .collabLabel(policy.collabLabel())
                .playDate(req.playDate())
                .sessionType(sessionType)
                .gameType(req.gameType())
                .buyInPerEntry(req.buyInPerEntry())
                .entries(req.entries())
                .discount(req.discount())
                .prize(req.prize())
                .notes(req.notes())
                .gtdAmount(req.gtdAmount())
                .fieldEntries(req.fieldEntries())
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

        String sessionType = normalizeSessionType(req.sessionType());

        VenuePolicy policy = normalizeVenuePolicy(
                sessionType,
                req.venueId(),
                req.isCollab(),
                req.collabLabel()
        );

        session.setVenueId(policy.venueId());
        session.setSessionType(sessionType);
        session.setPlayDate(req.playDate());
        session.setGameType(req.gameType());
        session.setBuyInPerEntry(req.buyInPerEntry());
        session.setEntries(req.entries());
        session.setDiscount(req.discount());
        session.setPrize(req.prize());
        session.setNotes(req.notes());
        session.setGtdAmount(req.gtdAmount());
        session.setFieldEntries(req.fieldEntries());

        // ✅ 콜라보 필드
        // (setter 이름이 다르면 여기만 맞춰 수정)
        session.setCollab(policy.isCollab());
        session.setCollabLabel(policy.collabLabel());

        session.recalc();

        return GameSessionResponse.from(session);
    }

    @Transactional
    public void delete(Long userId, Long sessionId) {
        var session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));

        gameSessionRepository.delete(session);
    }

    public GameSessionResponse getOne(Long userId, Long sessionId) {
        var session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "게임 세션을 찾을 수 없습니다."));
        return GameSessionResponse.from(session);
    }

    // =========================
    // 조회용
    // =========================

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

    // =========================
    // 내부 유틸
    // =========================

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

        // 2) 매장 / 타입
        if (GameSession.SESSION_TYPE_VENUE.equals(s.getSessionType())) {
            // 콜라보면 라벨 우선
            if (s.isCollab()) {
                if (s.getCollabLabel() != null && !s.getCollabLabel().isBlank()) {
                    parts.add("[콜라보] " + s.getCollabLabel());
                } else {
                    parts.add("[콜라보]");
                }
            } else {
                if (venueName != null && !venueName.isBlank()) {
                    parts.add(venueName);
                }
            }
        } else {
            // 온라인/대회/기타는 타입 태그만 붙여서 구분
            parts.add("[" + s.getSessionType() + "]");
        }

        return String.join(" · ", parts);
    }

    /**
     * sessionType null/공백이면 기본 VENUE 로 처리,
     * 대소문자 섞여 와도 upper-case 로 통일
     */
    private String normalizeSessionType(String raw) {
        if (raw == null || raw.isBlank()) {
            return GameSession.SESSION_TYPE_VENUE; // 기본값
        }
        String upper = raw.trim().toUpperCase();
        if (!ALLOWED_SESSION_TYPES.contains(upper)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_SESSION_TYPE",
                    "지원하지 않는 세션 타입입니다: " + raw
            );
        }
        return upper;
    }

    // -------------------------
    // ✅ VENUE 정책(단독 vs 콜라보)
    // -------------------------

    private record VenuePolicy(
            Long venueId,
            boolean isCollab,
            String collabLabel
    ) {}

    /**
     * 세션 타입/매장/콜라보 조합 검증 + 정규화
     *
     * 정책:
     * - VENUE 아님: venueId 금지, 콜라보 금지
     * - VENUE & 단독: venueId 필수, collabLabel null
     * - VENUE & 콜라보: venueId는 null로 강제, collabLabel 필수
     */
    private VenuePolicy normalizeVenuePolicy(String sessionType, Long venueId, Boolean isCollab, String collabLabel) {

        // non-VENUE: venueId 금지 + collab 금지
        if (!GameSession.SESSION_TYPE_VENUE.equals(sessionType)) {
            if (venueId != null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "VENUE_ID_NOT_ALLOWED",
                        "해당 세션 타입에는 venueId를 지정할 수 없습니다."
                );
            }
            if (Boolean.TRUE.equals(isCollab) || (collabLabel != null && !collabLabel.isBlank())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "COLLAB_NOT_ALLOWED",
                        "VENUE 타입이 아닌 세션에는 콜라보 정보를 지정할 수 없습니다."
                );
            }
            return new VenuePolicy(null, false, null);
        }

        // VENUE: collab 여부에 따라 정책 분기
        boolean collab = Boolean.TRUE.equals(isCollab);

        if (collab) {
            if (collabLabel == null || collabLabel.isBlank()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "COLLAB_LABEL_REQUIRED",
                        "콜라보 세션에는 collabLabel이 필요합니다."
                );
            }
            // 콜라보는 venueId를 null로 강제(단독 매장 통계에 섞이지 않게)
            return new VenuePolicy(null, true, collabLabel.trim());
        }

        // 단독 매장: venueId 필수
        if (venueId == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VENUE_ID_REQUIRED",
                    "VENUE 타입(단독) 세션에는 venueId가 필요합니다."
            );
        }

        return new VenuePolicy(venueId, false, null);
    }
}
