package com.rolling.pokerly.handreview.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.handreview.domain.HandReview;

public interface HandReviewRepository extends JpaRepository<HandReview, Long> {

    List<HandReview> findAllByUserIdOrderByCreatedAtDesc(Long userId);

}
