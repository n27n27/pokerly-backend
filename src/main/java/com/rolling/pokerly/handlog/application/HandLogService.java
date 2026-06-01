package com.rolling.pokerly.handlog.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.handlog.domain.HandLogBlindLevel;
import com.rolling.pokerly.handlog.domain.HandLogEvent;
import com.rolling.pokerly.handlog.domain.HandLogHand;
import com.rolling.pokerly.handlog.dto.HandLogBlindLevelCopyRequest;
import com.rolling.pokerly.handlog.dto.HandLogBlindLevelCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogBlindLevelResponse;
import com.rolling.pokerly.handlog.dto.HandLogEventCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogEventResponse;
import com.rolling.pokerly.handlog.dto.HandLogHandCreateRequest;
import com.rolling.pokerly.handlog.dto.HandLogHandMoveRequest;
import com.rolling.pokerly.handlog.dto.HandLogHandResponse;
import com.rolling.pokerly.handlog.repo.HandLogBlindLevelRepository;
import com.rolling.pokerly.handlog.repo.HandLogEventRepository;
import com.rolling.pokerly.handlog.repo.HandLogHandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HandLogService {

    private final HandLogEventRepository eventRepository;
    private final HandLogBlindLevelRepository blindLevelRepository;
    private final HandLogHandRepository handRepository;

    @Transactional(readOnly = true)
    public List<HandLogEventResponse> getMyEvents(Long userId) {
        return eventRepository.findAllByUserIdOrderByEventAtDescCreatedAtDesc(userId)
                .stream()
                .map(event -> {
                    var levels = getLevelResponses(userId, event.getId());
                    return HandLogEventResponse.from(event, levels);
                })
                .toList();
    }

    @Transactional
    public HandLogEventResponse createEvent(Long userId, HandLogEventCreateRequest req) {
        if (req.name() == null || req.name().trim().isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_NAME",
                    "대회명을 입력해 주세요.");
        }

        var event = HandLogEvent.builder()
                .userId(userId)
                .name(req.name().trim())
                .eventAt(LocalDateTime.now())
                .venueId(null)
                .build();

        var saved = eventRepository.save(event);
        return HandLogEventResponse.from(saved, List.of());
    }

    @Transactional(readOnly = true)
    public HandLogEventResponse getEventDetail(Long userId, Long eventId) {
        var event = getEventOrThrow(userId, eventId);
        var levels = getLevelResponses(userId, eventId);

        return HandLogEventResponse.from(event, levels);
    }

    @Transactional
    public HandLogEventResponse updateEvent(
            Long userId,
            Long eventId,
            HandLogEventCreateRequest req) {
        var event = getEventOrThrow(userId, eventId);

        if (req.name() == null || req.name().trim().isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_NAME",
                    "대회명을 입력해 주세요.");
        }

        event.updateName(req.name().trim());

        var levels = getLevelResponses(userId, eventId);
        return HandLogEventResponse.from(event, levels);
    }

    @Transactional
    public void deleteEvent(Long userId, Long eventId) {
        var event = getEventOrThrow(userId, eventId);

        boolean hasHands = handRepository.existsByUserIdAndEventId(userId, eventId);

        if (hasHands) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EVENT_HAS_HANDS",
                    "기록된 핸드가 있는 대회는 삭제할 수 없습니다.");
        }

        var levels = blindLevelRepository
                .findAllByUserIdAndEventIdOrderByLevelNoAscCreatedAtAsc(userId, eventId);

        blindLevelRepository.deleteAll(levels);
        eventRepository.delete(event);
    }

    @Transactional
    public HandLogBlindLevelResponse createBlindLevel(
            Long userId,
            Long eventId,
            HandLogBlindLevelCreateRequest req) {
        getEventOrThrow(userId, eventId);

        if (req.levelNo() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_LEVEL_NO",
                    "레벨을 입력해 주세요.");
        }

        var level = HandLogBlindLevel.builder()
                .userId(userId)
                .eventId(eventId)
                .levelNo(req.levelNo())
                .smallBlind(defaultNumber(req.smallBlind()))
                .bigBlind(defaultNumber(req.bigBlind()))
                .ante(defaultNumber(req.ante()))
                .build();

        var saved = blindLevelRepository.save(level);
        return HandLogBlindLevelResponse.from(saved, List.of());
    }

    @Transactional
    public HandLogEventResponse copyBlindLevelsFromEvent(
            Long userId,
            Long targetEventId,
            HandLogBlindLevelCopyRequest req) {
        if (req.sourceEventId() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_SOURCE_EVENT",
                    "불러올 대회를 선택해 주세요.");
        }

        if (targetEventId.equals(req.sourceEventId())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "SAME_EVENT_NOT_ALLOWED",
                    "같은 대회의 블라인드 구조는 불러올 수 없습니다.");
        }

        var targetEvent = getEventOrThrow(userId, targetEventId);
        getEventOrThrow(userId, req.sourceEventId());

        var targetLevels = blindLevelRepository
                .findAllByUserIdAndEventIdOrderByLevelNoAscCreatedAtAsc(userId, targetEventId);

        if (!targetLevels.isEmpty()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "TARGET_EVENT_ALREADY_HAS_LEVELS",
                    "이미 등록된 블라인드 구간이 있어 불러올 수 없습니다.");
        }

        var sourceLevels = blindLevelRepository
                .findAllByUserIdAndEventIdOrderByLevelNoAscCreatedAtAsc(userId, req.sourceEventId());

        if (sourceLevels.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "SOURCE_EVENT_HAS_NO_LEVELS",
                    "선택한 대회에 불러올 블라인드 구간이 없습니다.");
        }

        var copiedLevels = sourceLevels.stream()
                .map(source -> HandLogBlindLevel.builder()
                        .userId(userId)
                        .eventId(targetEventId)
                        .levelNo(source.getLevelNo())
                        .smallBlind(source.getSmallBlind())
                        .bigBlind(source.getBigBlind())
                        .ante(source.getAnte())
                        .build())
                .toList();

        blindLevelRepository.saveAll(copiedLevels);

        var levels = getLevelResponses(userId, targetEventId);
        return HandLogEventResponse.from(targetEvent, levels);
    }

    @Transactional(readOnly = true)
    public HandLogBlindLevelResponse getBlindLevelDetail(
            Long userId,
            Long eventId,
            Long blindLevelId) {
        getEventOrThrow(userId, eventId);

        var level = getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var hands = handRepository
                .findAllByUserIdAndBlindLevelIdOrderByCreatedAtAsc(userId, blindLevelId)
                .stream()
                .map(HandLogHandResponse::from)
                .toList();

        return HandLogBlindLevelResponse.from(level, hands);
    }

    @Transactional
    public HandLogBlindLevelResponse updateBlindLevel(
            Long userId,
            Long eventId,
            Long blindLevelId,
            HandLogBlindLevelCreateRequest req) {
        getEventOrThrow(userId, eventId);

        var level = getBlindLevelOrThrow(userId, eventId, blindLevelId);

        if (req.levelNo() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_LEVEL_NO",
                    "레벨을 입력해 주세요.");
        }

        level.update(
                req.levelNo(),
                defaultNumber(req.smallBlind()),
                defaultNumber(req.bigBlind()),
                defaultNumber(req.ante()));

        var hands = handRepository
                .findAllByUserIdAndBlindLevelIdOrderByCreatedAtAsc(userId, blindLevelId)
                .stream()
                .map(HandLogHandResponse::from)
                .toList();

        return HandLogBlindLevelResponse.from(level, hands);
    }

    @Transactional
    public void deleteBlindLevel(
            Long userId,
            Long eventId,
            Long blindLevelId) {
        getEventOrThrow(userId, eventId);

        var level = getBlindLevelOrThrow(userId, eventId, blindLevelId);

        boolean hasHands = handRepository.existsByUserIdAndEventIdAndBlindLevelId(
                userId,
                eventId,
                blindLevelId);

        if (hasHands) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "BLIND_LEVEL_HAS_HANDS",
                    "이 블라인드 구간에 기록된 핸드가 있어 삭제할 수 없습니다.");
        }

        blindLevelRepository.delete(level);
    }

    @Transactional
    public HandLogHandResponse createHand(
            Long userId,
            Long eventId,
            Long blindLevelId,
            HandLogHandCreateRequest req) {
        getEventOrThrow(userId, eventId);
        getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var holeCards = resolveHoleCards(req);

        if (holeCards == null || holeCards.trim().isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_HOLE_CARDS",
                    "핸드를 입력해 주세요.");
        }

        var hand = HandLogHand.builder()
                .userId(userId)
                .eventId(eventId)
                .blindLevelId(blindLevelId)

                .holeCards(holeCards.trim())
                .firstRank(req.firstRank())
                .secondRank(req.secondRank())
                .suited(Boolean.TRUE.equals(req.suited()))

                .position(req.position())

                .actionType(req.actionType())
                .actionLabel(req.actionLabel())
                .preflopAllIn(Boolean.TRUE.equals(req.preflopAllIn()))

                .resultType(req.resultType() == null ? "NOT_RECORDED" : req.resultType())
                .resultLabel(req.resultLabel())

                .reviewRequired(Boolean.TRUE.equals(req.reviewRequired()))
                .memo(req.memo())

                .handStrengthTier(req.handStrengthTier())
                .handStrengthLabel(req.handStrengthLabel())
                .handStrengthColor(req.handStrengthColor())

                .build();

        var saved = handRepository.save(hand);
        return HandLogHandResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public HandLogHandResponse getHandDetail(
            Long userId,
            Long eventId,
            Long blindLevelId,
            Long handId) {
        getEventOrThrow(userId, eventId);
        getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var hand = getHandOrThrow(userId, eventId, blindLevelId, handId);

        return HandLogHandResponse.from(hand);
    }

    @Transactional
    public HandLogHandResponse updateHand(
            Long userId,
            Long eventId,
            Long blindLevelId,
            Long handId,
            HandLogHandCreateRequest req) {
        getEventOrThrow(userId, eventId);
        getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var hand = getHandOrThrow(userId, eventId, blindLevelId, handId);

        var holeCards = resolveHoleCards(req);

        if (holeCards == null || holeCards.trim().isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_HOLE_CARDS",
                    "핸드를 입력해 주세요.");
        }

        hand.update(
                holeCards.trim(),
                req.firstRank(),
                req.secondRank(),
                req.suited(),
                req.position(),
                req.actionType(),
                req.actionLabel(),
                req.preflopAllIn(),
                req.resultType(),
                req.resultLabel(),
                req.reviewRequired(),
                req.memo(),
                req.handStrengthTier(),
                req.handStrengthLabel(),
                req.handStrengthColor());

        return HandLogHandResponse.from(hand);
    }

    @Transactional
    public HandLogHandResponse moveHand(
            Long userId,
            Long eventId,
            Long blindLevelId,
            Long handId,
            HandLogHandMoveRequest req) {
        if (req.targetBlindLevelId() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TARGET_BLIND_LEVEL",
                    "이동할 블라인드 구간을 선택해 주세요.");
        }

        getEventOrThrow(userId, eventId);
        getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var targetLevel = getBlindLevelOrThrow(userId, eventId, req.targetBlindLevelId());
        var hand = getHandOrThrow(userId, eventId, blindLevelId, handId);

        if (blindLevelId.equals(targetLevel.getId())) {
            return HandLogHandResponse.from(hand);
        }

        hand.moveToBlindLevel(targetLevel.getId());

        return HandLogHandResponse.from(hand);
    }

    @Transactional
    public void deleteHand(
            Long userId,
            Long eventId,
            Long blindLevelId,
            Long handId) {
        getEventOrThrow(userId, eventId);
        getBlindLevelOrThrow(userId, eventId, blindLevelId);

        var hand = getHandOrThrow(userId, eventId, blindLevelId, handId);

        handRepository.delete(hand);
    }

    private HandLogEvent getEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "대회를 찾을 수 없습니다."));
    }

    private HandLogBlindLevel getBlindLevelOrThrow(Long userId, Long eventId, Long blindLevelId) {
        return blindLevelRepository.findByIdAndUserIdAndEventId(blindLevelId, userId, eventId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "BLIND_LEVEL_NOT_FOUND",
                        "블라인드 구간을 찾을 수 없습니다."));
    }

    private HandLogHand getHandOrThrow(
            Long userId,
            Long eventId,
            Long blindLevelId,
            Long handId) {
        return handRepository
                .findByIdAndUserIdAndEventIdAndBlindLevelId(handId, userId, eventId, blindLevelId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "HAND_NOT_FOUND",
                        "핸드 기록을 찾을 수 없습니다."));
    }

    private List<HandLogBlindLevelResponse> getLevelResponses(Long userId, Long eventId) {
        var levels = blindLevelRepository
                .findAllByUserIdAndEventIdOrderByLevelNoAscCreatedAtAsc(userId, eventId);

        var hands = handRepository.findAllByUserIdAndEventIdOrderByCreatedAtAsc(userId, eventId);

        Map<Long, List<HandLogHandResponse>> handsByLevelId = hands.stream()
                .collect(Collectors.groupingBy(
                        HandLogHand::getBlindLevelId,
                        Collectors.mapping(HandLogHandResponse::from, Collectors.toList())));

        return levels.stream()
                .map(level -> HandLogBlindLevelResponse.from(
                        level,
                        handsByLevelId.getOrDefault(level.getId(), List.of())))
                .toList();
    }

    private String resolveHoleCards(HandLogHandCreateRequest req) {
        if (req.holeCards() != null && !req.holeCards().trim().isEmpty()) {
            return req.holeCards();
        }

        return req.hand();
    }

    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}