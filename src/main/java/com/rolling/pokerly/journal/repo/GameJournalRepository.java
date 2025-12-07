package com.rolling.pokerly.journal.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.journal.domain.GameJournal;

public interface GameJournalRepository extends JpaRepository<GameJournal, Long> {

    Optional<GameJournal> findByUserIdAndJournalDate(Long userId, LocalDate journalDate);

    List<GameJournal> findByUserIdAndJournalDateBetween(Long userId, LocalDate start, LocalDate end);

    List<GameJournal> findByUserId(Long userId);
}
