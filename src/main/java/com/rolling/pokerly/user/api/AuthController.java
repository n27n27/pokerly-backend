package com.rolling.pokerly.user.api;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.security.jwt.JwtTokenProvider;
import com.rolling.pokerly.user.application.UserService;
import com.rolling.pokerly.user.dto.AuthResponse;
import com.rolling.pokerly.user.dto.LoginRequest;
import com.rolling.pokerly.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager; // SecurityConfig에서 주입
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        var token = new UsernamePasswordAuthenticationToken(request.getNickname(), request.getPassword());
        authenticationManager.authenticate(token);

        var u = userService.loadUser(request.getNickname());
        var access = tokenProvider.createAccessToken(u.getNickname(), u.getRole());
        var refresh = tokenProvider.createRefreshToken(u.getNickname());
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(UserResponse.from(u))
                .build();
    }

    // 아주 단순한 리프레시 예시 (실서비스에선 Refresh 저장/블랙리스트 고려)
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam("token") String refreshToken) {
        if (!tokenProvider.validate(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        var auth = tokenProvider.getAuthentication(refreshToken);
        var nickname = auth.getName();
        var u = userService.loadUser(nickname);

        var newAccess = tokenProvider.createAccessToken(u.getNickname(), u.getRole());
        var newRefresh = tokenProvider.createRefreshToken(u.getNickname());

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .user(UserResponse.from(u))
                .build();
    }
}
