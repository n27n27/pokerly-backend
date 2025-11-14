package com.rolling.pokerly.venue.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.venue.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByUserIdOrderByNameAsc(Long userId);

    Optional<Venue> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
