package com.rolling.pokerly.point.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.point.domain.PointTransaction;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Optional<PointTransaction> findTopByUserIdAndVenueIdOrderByIdDesc(Long userId, Long venueId);

    List<PointTransaction> findByUserIdAndVenueIdOrderByIdDesc(Long userId, Long venueId);

    boolean existsByUserIdAndVenueId(Long userId, Long venueId);
}
