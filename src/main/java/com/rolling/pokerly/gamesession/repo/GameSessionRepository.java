package com.rolling.pokerly.gamesession.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.gamesession.domain.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    Optional<GameSession> findByIdAndUserId(Long id, Long userId);

    List<GameSession> findByUserIdAndPlayDateBetweenOrderByPlayDateAsc(
            Long userId,
            LocalDate start,
            LocalDate end
    );

    List<GameSession> findByUserIdAndVenueIdAndPlayDateBetweenOrderByPlayDateAsc(
            Long userId,
            Long venueId,
            LocalDate from,
            LocalDate to
    );

    boolean existsByUserIdAndVenueId(Long userId, Long venueId);
}
