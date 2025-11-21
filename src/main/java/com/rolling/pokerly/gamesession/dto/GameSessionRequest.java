package com.rolling.pokerly.gamesession.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * 게임 세션 생성 및 수정을 위한 요청 DTO
 */
@Getter
@Setter
public class GameSessionRequest {

    private Long venueId;

    private LocalDate playDate;

    private String gameType;   // "GTD" | "데일리" | "기타"

    private Long buyInPerEntry;  // 1회 바인 금액

    private Integer entries;     // 엔트리 수

    private Long discount;       // 총 할인

    private Long prize;          // 상금

    private String notes;
}
