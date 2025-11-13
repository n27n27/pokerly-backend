package com.rolling.pokerly.gamesession.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.gamesession.domain.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByUserIdAndPlayDateBetween(Long userId, LocalDate from, LocalDate to);
}
