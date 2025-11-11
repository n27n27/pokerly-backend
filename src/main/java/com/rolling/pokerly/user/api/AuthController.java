package com.rolling.pokerly.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.core.exception.ApiException;
import com.rolling.pokerly.security.jwt.JwtTokenProvider;
import com.rolling.pokerly.user.application.RefreshTokenService;
import com.rolling.pokerly.user.application.UserService;
import com.rolling.pokerly.user.dto.AuthResponse;
import com.rolling.pokerly.user.dto.LoginRequest;
import com.rolling.pokerly.user.dto.RefreshRequest;
import com.rolling.pokerly.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager; // SecurityConfig에서 주입
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        var token = new UsernamePasswordAuthenticationToken(request.getNickname(), request.getPassword());
        authenticationManager.authenticate(token);

        var u = userService.loadUser(request.getNickname());
        var access = tokenProvider.createAccessToken(u.getNickname(), u.getRole());
        var refresh = tokenProvider.createRefreshToken(u.getNickname());
        var refreshExp = tokenProvider.extractExpiry(refresh);

        refreshTokenService.save(u.getNickname(),
                                    refresh,
                                    refreshExp);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(UserResponse.from(u))
                .build();
    }

    // 아주 단순한 리프레시 예시 (실서비스에선 Refresh 저장/블랙리스트 고려)
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest req) {
        // 1) 저장된 refresh와 일치 & 미만료 확인
        if (!refreshTokenService.validate(req.getNickname(), req.getRefreshToken())) {
            throw new ApiException("Invalid or expired refresh token");
        }

        var u = userService.loadUser(req.getNickname());

        var newAccess = tokenProvider.createAccessToken(u.getNickname(), u.getRole());
        var newRefresh = tokenProvider.createRefreshToken(u.getNickname());

        var exp = tokenProvider.extractExpiry(newRefresh);
        refreshTokenService.save(u.getNickname(), newRefresh, exp);

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .user(UserResponse.from(u))
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        var nickname = authentication.getName();   // JWT subject = nickname
        refreshTokenService.delete(nickname);      // DB에서 리프레시 토큰 제거
        return ResponseEntity.noContent().build(); // 204
    }

}
