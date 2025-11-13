package com.rolling.pokerly.user.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rolling.pokerly.user.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByNickname(String nickname);
    void deleteByNickname(String nickname);
}
