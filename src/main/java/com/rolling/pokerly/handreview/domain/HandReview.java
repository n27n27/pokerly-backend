package com.rolling.pokerly.handreview.domain;

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
@Table(name = "hand_reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "hero_hand", nullable = false, length = 10)
    private String heroHand;

    @Column(length = 20)
    private String position;

    @Column(length = 50)
    private String blinds;

    @Column(name = "stack_bb")
    private Integer stackBb;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String question;

    @Column(name = "simple_main_street", length = 20)
    private String simpleMainStreet;

    @Column(name = "simple_pot_type", length = 20)
    private String simplePotType;

    @Column(name = "simple_board_texture", length = 20)
    private String simpleBoardTexture;

    @Column(name = "simple_hero_strength", length = 20)
    private String simpleHeroStrength;

    @Column(name = "simple_hero_line", length = 20)
    private String simpleHeroLine;

    @Column(name = "analysis_simple_json", columnDefinition = "TEXT")
    private String analysisSimpleJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
