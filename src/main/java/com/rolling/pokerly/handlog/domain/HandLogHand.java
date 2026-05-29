package com.rolling.pokerly.handlog.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hand_log_hands")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandLogHand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hand_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "blind_level_id", nullable = false)
    private Long blindLevelId;

    @Column(name = "hole_cards", nullable = false, length = 10)
    private String holeCards;

    @Column(name = "first_rank", length = 1)
    private String firstRank;

    @Column(name = "second_rank", length = 1)
    private String secondRank;

    @Column(nullable = false)
    private Boolean suited;

    @Column(length = 20)
    private String position;

    @Column(name = "action_type", length = 30)
    private String actionType;

    @Column(name = "action_label", length = 30)
    private String actionLabel;

    @Column(name = "preflop_all_in", nullable = false)
    private Boolean preflopAllIn;

    @Column(name = "result_type", length = 30)
    private String resultType;

    @Column(name = "result_label", length = 30)
    private String resultLabel;

    @Column(name = "review_required", nullable = false)
    private Boolean reviewRequired;

    @Column(length = 1000)
    private String memo;

    @Column(name = "hand_strength_tier", length = 30)
    private String handStrengthTier;

    @Column(name = "hand_strength_label", length = 30)
    private String handStrengthLabel;

    @Column(name = "hand_strength_color", length = 30)
    private String handStrengthColor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}