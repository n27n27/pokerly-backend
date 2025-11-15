package com.rolling.pokerly.point.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Column(name = "change_amount", nullable = false)
    private Long changeAmount;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "type", nullable = false, length = 30)
    private String type; // "EARN", "USE", "ADJUST"

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PointTransaction(
            Long id,
            Long userId,
            Long venueId,
            Long gameSessionId,
            Long changeAmount,
            Long balanceAfter,
            String type,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.venueId = venueId;
        this.gameSessionId = gameSessionId;
        this.changeAmount = changeAmount;
        this.balanceAfter = balanceAfter;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
