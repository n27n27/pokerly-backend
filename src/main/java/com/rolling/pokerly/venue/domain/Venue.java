package com.rolling.pokerly.venue.domain;

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
@Table(name = "venues")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long createdByUserId;

    private String name;
    private String location;
    private String notes;

    // 매장 타입 (추후 확정될 때 ENUM 대체 가능)
    private String type;

    // ⭐ 포인트 잔액 추가
    @Column(name = "point_balance", nullable = false)
    private Long pointBalance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // 최초 생성 시 자동 설정
    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        var now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;

        // type 기본값 설정
        if (type == null) {
            type = "USER_PRIVATE";
        }

        // pointBalance 기본값
        if (pointBalance == null) {
            pointBalance = 0L;
        }
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 기존 update 확장 — pointBalance 추가
    public void update(String name, String location, String notes, Long pointBalance) {
        this.name = name;
        this.location = location;
        this.notes = notes;

        this.pointBalance = pointBalance != null ? pointBalance : this.pointBalance;
    }
}
