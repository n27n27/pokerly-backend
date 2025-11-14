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

    @Column(length = 1000)
    private String notes;

    @Column(name = "profit_cash_realized", nullable = false)
    private Long profitCashRealized;

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
     * 할인 로직에 대한 기본 가정:
     * - total_cash_in / total_point_in : "표면상 바인 총액"
     * - discount : 그 중 실제로 깎인 금액 (현금 기준으로 가정)
     *
     * EV(포인트 포함) = cash_out - (total_cash_in + total_point_in - discount)
     * 현금 기준 손익 = cash_out - (total_cash_in - discount)
     */
    private void recalcProfit() {
        long effectiveTotalIn = (totalCashIn + totalPointIn) - discount;
        this.profitIncludingPoints = cashOut - effectiveTotalIn;

        long effectiveCashIn = totalCashIn - discount; // 할인은 현금에 먼저 적용된다고 가정
        this.profitCashRealized = cashOut - effectiveCashIn;
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
        this.notes = notes;
    }
}
