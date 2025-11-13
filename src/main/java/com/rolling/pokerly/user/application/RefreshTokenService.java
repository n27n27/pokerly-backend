package com.rolling.pokerly.user.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rolling.pokerly.user.domain.RefreshToken;
import com.rolling.pokerly.user.repo.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void save(String nickname, String token, LocalDateTime expiresAt) {
        var existing = refreshTokenRepository.findByNickname(nickname);
        if (existing.isPresent()) {
            var rt = existing.get();
            rt.setToken(token);
            rt.setExpiresAt(expiresAt);
            refreshTokenRepository.save(rt);
        } else {
            var rt = RefreshToken.builder()
                    .nickname(nickname)
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.save(rt);
        }
    }

    @Transactional(readOnly = true)
    public boolean validate(String nickname, String token) {
        var stored = refreshTokenRepository.findByNickname(nickname)
                .orElse(null);
        if (stored == null) return false;
        return stored.getToken().equals(token)
                && stored.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void delete(String nickname) {
        refreshTokenRepository.deleteByNickname(nickname);
    }
}
