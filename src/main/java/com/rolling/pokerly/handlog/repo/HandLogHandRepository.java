package com.rolling.pokerly.handlog.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.handlog.domain.HandLogHand;

public interface HandLogHandRepository extends JpaRepository<HandLogHand, Long> {

    List<HandLogHand> findAllByUserIdAndEventIdOrderByCreatedAtAsc(
            Long userId,
            Long eventId
    );

    List<HandLogHand> findAllByUserIdAndBlindLevelIdOrderByCreatedAtAsc(
            Long userId,
            Long blindLevelId
    );
}