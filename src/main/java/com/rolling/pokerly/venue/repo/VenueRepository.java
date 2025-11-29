package com.rolling.pokerly.venue.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.venue.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    // 1) 유저별 매장 리스트 조회 (이름순)
    List<Venue> findByCreatedByUserIdOrderByNameAsc(Long createdByUserId);

    // 2) 유저가 소유한 특정 venue 조회
    Optional<Venue> findByIdAndCreatedByUserId(Long id, Long createdByUserId);

    // 3) 유저가 같은 이름으로 venue를 이미 가지고 있는지 체크
    boolean existsByCreatedByUserIdAndName(Long createdByUserId, String name);

    @Override
    Optional<Venue> findById(Long id);
}
