package com.rolling.pokerly.handlog.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.handlog.domain.HandLogEvent;

public interface HandLogEventRepository extends JpaRepository<HandLogEvent, Long> {

    List<HandLogEvent> findAllByUserIdOrderByEventAtDescCreatedAtDesc(Long userId);

    Optional<HandLogEvent> findByIdAndUserId(Long id, Long userId);
}