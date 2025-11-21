package com.rolling.pokerly.gamesession.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
import lombok.Setter;

@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "play_date")
    private LocalDate playDate;

    private String title;

    // "GTD", "데일리", "기타"
    @Column(name = "game_type")
    private String gameType;

    @Column(name = "buy_in_per_entry")
    private Long buyInPerEntry;

    @Column
    private Integer entries;

    @Column
    private Long discount;

    // 계산: buy_in_per_entry * entries - discount
    @Column(name = "total_buy_in")
    private Long totalBuyIn;

    // 그날 머니인 금액
    @Column
    private Long prize;

    // 계산: prize - totalBuyIn
    @Column(name = "net_profit")
    private Long netProfit;

    @Column
    private String notes;

     @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * buyInPerEntry, entries, discount, prize 값을 기반으로
     * totalBuyIn / netProfit 을 다시 계산한다.
     */
    public void recalc() {

        long safeBuyInPerEntry = Objects.requireNonNullElse(buyInPerEntry, 0L);
        int safeEntries = Objects.requireNonNullElse(entries, 0);
        long safeDiscount = Objects.requireNonNullElse(buyInPerEntry, 0L);
        long safePrize = Objects.requireNonNullElse(prize, 0L);

        this.totalBuyIn = safeBuyInPerEntry * safeEntries - safeDiscount;
        if (this.totalBuyIn < 0) {
            this.totalBuyIn = 0L; // 방어 코드
        }
        this.netProfit = safePrize - this.totalBuyIn;
    }
}
