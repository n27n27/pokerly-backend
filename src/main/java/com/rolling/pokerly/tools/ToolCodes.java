package com.rolling.pokerly.tools;

/**
 * Tools(도구) 기능에서 사용하는 도구 코드 상수 모음.
 *
 * DB tool_usage_event.tool_code 컬럼과 1:1 매칭되는 값들이다.
 */
public final class ToolCodes {

    private ToolCodes() {
    }

    /** 콜 EV 계산기 */
    public static final String CALL_EV = "CALL_EV";

    /** 토너먼트 EV(GTD/오버레이) 계산기 */
    public static final String TOURNAMENT_EV = "TOURNAMENT_EV";

    /** 리엔트리 EV 계산기 */
    public static final String REENTRY_EV = "REENTRY_EV";

    /** Iso / 3Bet 사이즈 계산기 */
    public static final String ISO_3BET = "ISO_3BET";

    /** ICM 계산기 */
    public static final String ICM = "ICM";

    /** SPR 계산기 */
    public static final String SPR = "SPR";

    /** Implied Odds 계산기 */
    public static final String IMPLIED_ODDS = "IMPLIED_ODDS";
}
