package com.rolling.pokerly.handreview.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.handreview.domain.HandReview;
import com.rolling.pokerly.handreview.dto.HandReviewResponse;
import com.rolling.pokerly.handreview.dto.SimpleAnalyzeRequest;
import com.rolling.pokerly.handreview.repo.HandReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HandReviewStoreService {

    private final HandReviewSimpleService simpleService;
    private final HandReviewRepository repository;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public HandReviewResponse analyzeAndCreate(Long userId, SimpleAnalyzeRequest req) {

        var simple = simpleService.analyze(req);

        String json;
        try {
            json = om.writeValueAsString(simple);
        } catch (JsonProcessingException e) {
            throw new ApiException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "JSON_SERIALIZE_ERROR",
            "Simple 분석 결과를 저장하는 중 오류가 발생했습니다."
    );
        }

        var entity = HandReview.builder()
                .userId(userId)
                .sessionId(req.getSessionId())
                .title(req.getTitle())
                .heroHand(req.getHeroHand())
                .position(req.getPosition())
                .blinds(req.getBlinds())
                .stackBb(req.getStackBb())
                .description(req.getDescription())
                .question(req.getQuestion())
                .simpleMainStreet(req.getSimpleMainStreet())
                .simplePotType(req.getSimplePotType())
                .simpleBoardTexture(req.getSimpleBoardTexture())
                .simpleHeroStrength(req.getSimpleHeroStrength())
                .simpleHeroLine(req.getSimpleHeroLine())
                .analysisSimpleJson(json)
                .build();

        var saved = repository.save(entity);
        return HandReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public java.util.List<HandReviewResponse> getMyHands(Long userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(HandReviewResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HandReviewResponse getMyHand(Long userId, Long id) {
        var h = repository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "NOT_FOUND",
                                "핸드 기록을 찾을 수 없습니다."
                        ));

        if (!h.getUserId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "해당 핸드에 접근할 권한이 없습니다."
            );
        }

        return HandReviewResponse.from(h);
    }

}
