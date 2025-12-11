package com.rolling.pokerly.tools.dto;

/**
 * 도구 사용 로깅 요청 DTO.
 *
 * toolCode 값은 아래 중 하나를 사용한다.
 *  - "CALL_EV"       : 콜 EV 계산기
 *  - "TOURNAMENT_EV" : 토너먼트 EV(GTD / 오버레이) 계산기
 *  - "REENTRY_EV"    : 리엔트리 EV 계산기
 *  - "ISO_3BET"      : Iso / 3Bet 사이즈 계산기
 *  - "ICM"           : ICM 계산기
 *  - "SPR"           : SPR 계산기
 *  - "IMPLIED_ODDS"  : Implied Odds 계산기
 *
 * action 값 예시:
 *  - "OPEN"       : 페이지 진입
 *  - "CALCULATE"  : 실제 계산 버튼 실행
 */
public record ToolUsageRequest(
        String toolCode,
        String action
) { }
