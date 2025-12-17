package com.rolling.pokerly.gamesession.dto;

import java.time.LocalDate;

/**
 * 게임 세션 생성 및 수정을 위한 요청 DTO
 */
public record GameSessionRequest (

        Long venueId,          // VENUE 타입일 때만 사용
        LocalDate playDate,
        String sessionType,    // VENUE / MAJOR / ONLINE / OTHER
        String gameType,       // "GTD", "데일리", "기타" 등

        Long buyInPerEntry,
        Integer entries,
        Long discount,
        Long prize,
        String notes,

        Long gtdAmount,        // 광고된 GTD 금액 (옵션)
        Integer fieldEntries,   // 토너 전체 엔트리 수 (옵션)

        Boolean isCollab,         // 협업 세션 여부
        String collabLabel        // 협업 세션 라벨
) { }