package com.rolling.pokerly.tools.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_usage_event")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ToolUsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // users.id 참조 (FK는 선택)
    private Long userId;

    /**
     * 사용한 도구 코드.
     * CALL_EV, TOURNAMENT_EV, REENTRY_EV, ISO_3BET, ICM, SPR, IMPLIED_ODDS 중 하나.
     */
    @Column(length = 50, nullable = false)
    private String toolCode;

    /**
     * 동작 유형.
     * 예: OPEN(페이지 진입), CALCULATE(계산 버튼 실행)
     */
    @Column(length = 30, nullable = false)
    private String action;

    private LocalDateTime usedAt;

}
