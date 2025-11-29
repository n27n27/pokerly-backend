package com.rolling.pokerly.handreview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAnalyzeResponse {

    // 프리플랍 코멘트
    private String preflopRecommendation;
    private String preflopDetail;

    // 플랍/턴 등 후속 스팟 코멘트
    private String postflopComment;

    // 전체 종합
    private String overallSimpleComment;
}
