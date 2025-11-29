package com.rolling.pokerly.handreview.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.handreview.domain.HandReview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandReviewResponse {

    private Long id;
    private Long sessionId;

    private String title;
    private String heroHand;
    private String position;
    private String blinds;
    private Integer stackBb;
    private String description;
    private String question;

    private String simpleMainStreet;
    private String simplePotType;
    private String simpleBoardTexture;
    private String simpleHeroStrength;
    private String simpleHeroLine;

    private SimpleAnalyzeResponse simpleAnalysis; // 파싱된 분석
    private String rawJson;                       // 원본 JSON

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HandReviewResponse from(HandReview e) {
        ObjectMapper om = new ObjectMapper();
        SimpleAnalyzeResponse parsed = null;

        try {
            if (e.getAnalysisSimpleJson() != null) {
                parsed = om.readValue(e.getAnalysisSimpleJson(), SimpleAnalyzeResponse.class);
            }
        } catch (JsonProcessingException ignored) {
            throw new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "JSON_SERIALIZE_ERROR",
                "Simple 분석 결과를 저장하는 중 오류가 발생했습니다."
            );
        }

        return HandReviewResponse.builder()
                .id(e.getId())
                .sessionId(e.getSessionId())
                .title(e.getTitle())
                .heroHand(e.getHeroHand())
                .position(e.getPosition())
                .blinds(e.getBlinds())
                .stackBb(e.getStackBb())
                .description(e.getDescription())
                .question(e.getQuestion())
                .simpleMainStreet(e.getSimpleMainStreet())
                .simplePotType(e.getSimplePotType())
                .simpleBoardTexture(e.getSimpleBoardTexture())
                .simpleHeroStrength(e.getSimpleHeroStrength())
                .simpleHeroLine(e.getSimpleHeroLine())
                .simpleAnalysis(parsed)
                .rawJson(e.getAnalysisSimpleJson())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
