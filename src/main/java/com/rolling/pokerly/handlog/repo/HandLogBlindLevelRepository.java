package com.rolling.pokerly.handlog.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.handlog.domain.HandLogBlindLevel;

public interface HandLogBlindLevelRepository extends JpaRepository<HandLogBlindLevel, Long> {

    List<HandLogBlindLevel> findAllByUserIdAndEventIdOrderByLevelNoAscCreatedAtAsc(
            Long userId,
            Long eventId
    );

    Optional<HandLogBlindLevel> findByIdAndUserIdAndEventId(
            Long id,
            Long userId,
            Long eventId
    );
}