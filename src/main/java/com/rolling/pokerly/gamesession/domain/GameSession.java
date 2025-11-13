package com.rolling.pokerly.gamesession.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    // DDL 관련 애노테이션 금지: nullable/length 등 지정 안함
    private Long userId;
    private Long venueId;
    private LocalDate playDate;

    private String title;        // 예: "10만 바인 2K GTD"
    private String gameType;     // 예: "MTT", "Cash" 등 문자열

    private BigDecimal buyIn;                // 1회 바인 금액
    private Integer entries;                 // 리바인 포함 총 엔트리 수
    private BigDecimal cashOut;              // 실현 현금
    private BigDecimal pointUsed;            // 사용한 포인트 금액
    private BigDecimal pointRemainAfter;     // 게임 종료 시점 포인트 잔액
    private BigDecimal discount;             // 얼리/프로모션 등 총 할인
    private String notes;

    // 파생치(조회 성능용, 서비스에서 계산/저장)
    private BigDecimal profitCashRealized;     // cash_out - (buyIn*entries - discount)
    private BigDecimal profitIncludingPoints;  // cash_out - (buyIn*entries + pointUsed - discount)
}
