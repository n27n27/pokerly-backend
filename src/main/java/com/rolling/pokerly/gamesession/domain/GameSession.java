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

    public static final String SESSION_TYPE_VENUE = "VENUE";
    public static final String SESSION_TYPE_MAJOR = "MAJOR";
    public static final String SESSION_TYPE_ONLINE = "ONLINE";
    public static final String SESSION_TYPE_OTHER = "OTHER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "play_date")
    private LocalDate playDate;

    @Column(name = "session_type")
    private String sessionType; // VENUE / MAJOR / ONLINE / OTHER

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

    // 광고된 GTD 금액 (옵션)
    @Column(name = "gtd_amount")
    private Long gtdAmount;

    // 토너 전체 엔트리 수 (옵션)
    @Column(name = "field_entries")
    private Integer fieldEntries;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_collab", nullable = false)
    private boolean collab;           // 협업 세션 여부

    @Column(name = "collab_label", length = 50)
    private String collabLabel;       // 협업 세션 라벨

    /**
     * buyInPerEntry, entries, discount, prize 값을 기반으로
     * totalBuyIn / netProfit 을 다시 계산한다.
     */
    public void recalc() {

        long safeBuyInPerEntry = Objects.requireNonNullElse(buyInPerEntry, 0L);
        int safeEntries = Objects.requireNonNullElse(entries, 0);
        long safeDiscount = Objects.requireNonNullElse(discount, 0L);
        long safePrize = Objects.requireNonNullElse(prize, 0L);

        this.totalBuyIn = safeBuyInPerEntry * safeEntries - safeDiscount;
        if (this.totalBuyIn < 0) {
            this.totalBuyIn = 0L; // 방어 코드
        }
        this.netProfit = safePrize - this.totalBuyIn;
    }
}
