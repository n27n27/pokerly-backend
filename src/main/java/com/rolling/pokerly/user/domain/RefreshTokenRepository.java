package com.rolling.pokerly.user.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByNickname(String nickname);
    void deleteByNickname(String nickname);
}
