package com.rolling.pokerly.gamesession.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_sessions")
@Getter
@NoArgsConstructor
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "play_date", nullable = false)
    private LocalDate playDate;

    @Column(length = 100)
    private String title;

    @Column(name = "game_type", length = 30)
    private String gameType;

    @Column(name = "total_cash_in", nullable = false)
    private Long totalCashIn;

    @Column(name = "total_point_in", nullable = false)
    private Long totalPointIn;

    @Column(nullable = false)
    private Integer entries;

    @Column(name = "cash_out", nullable = false)
    private Long cashOut;

    @Column(name = "discount", nullable = false)
    private Long discount;

    @Column(name = "earned_point", nullable = false)
    private Long earnedPoint;   // 세션 중 적립된 포인트(현금 가치 기준)

    @Column(length = 1000)
    private String notes;

    // 현금 기준 손익: cash_out - (total_cash_in - discount)
    @Column(name = "profit_cash_realized", nullable = false)
    private Long profitCashRealized;

    // EV 기준 손익(현금 + 포인트): profit_cash_realized + earned_point
    @Column(name = "profit_including_points", nullable = false)
    private Long profitIncludingPoints;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private GameSession(
            Long id,
            Long userId,
            Long venueId,
            LocalDate playDate,
            String title,
            String gameType,
            Long totalCashIn,
            Long totalPointIn,
            Integer entries,
            Long cashOut,
            Long discount,
            Long earnedPoint,
            String notes,
            Long profitCashRealized,
            Long profitIncludingPoints,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.venueId = venueId;
        this.playDate = playDate;
        this.title = title;
        this.gameType = gameType;
        this.totalCashIn = totalCashIn;
        this.totalPointIn = totalPointIn;
        this.entries = entries;
        this.cashOut = cashOut;
        this.discount = discount;
        this.earnedPoint = earnedPoint;
        this.notes = notes;
        this.profitCashRealized = profitCashRealized;
        this.profitIncludingPoints = profitIncludingPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        recalcProfit();
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalcProfit();
    }

    /**
     * EV/Profit 정의 :
     *
     * - 포인트를 받는 것이 위닝/EV의 핵심
     * - 포인트 사용(point_in)은 과거에 얻은 자산을 사용하는 것일 뿐,
     *   오늘 세션의 profit 자체가 아님 → EV 계산에서는 제외
     * - 캐시아웃은 현금 흐름
     *
     * 현금 기준 손익:
     *   profitCashRealized = cash_out - (total_cash_in - discount)
     *
     * EV 기준 손익(현금 + 포인트):
     *   profitIncludingPoints = profitCashRealized + earned_point
     */
    private void recalcProfit() {
        long effectiveCashIn = totalCashIn - discount;
        this.profitCashRealized = cashOut - effectiveCashIn;
        this.profitIncludingPoints = this.profitCashRealized + earnedPoint;
    }

    public void update(
            LocalDate playDate,
            Long venueId,
            String title,
            String gameType,
            Long totalCashIn,
            Long totalPointIn,
            Integer entries,
            Long cashOut,
            Long discount,
            Long earnedPoint,
            String notes
    ) {
        this.playDate = playDate;
        this.venueId = venueId;
        this.title = title;
        this.gameType = gameType;
        this.totalCashIn = totalCashIn;
        this.totalPointIn = totalPointIn;
        this.entries = entries;
        this.cashOut = cashOut;
        this.discount = discount;
        this.earnedPoint = earnedPoint;
        this.notes = notes;
    }
}
