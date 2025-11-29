package com.rolling.pokerly.handreview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAnalyzeRequest {
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
}
