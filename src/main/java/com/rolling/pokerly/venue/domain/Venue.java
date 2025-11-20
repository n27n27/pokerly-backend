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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venues")
@Getter
@NoArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long createdByUserId;

    private String name;
    private String location;
    private String notes;

    private String type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Venue(Long id, Long createdByUserId, String name, String location, String notes,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdByUserId = createdByUserId;
        this.name = name;
        this.location = location;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        var now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;

        if (type == null) {
            type = "USER_PRIVATE";
        }
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String name, String location, String notes) {
        this.name = name;
        this.location = location;
        this.notes = notes;
    }
}
