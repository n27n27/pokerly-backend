package com.rolling.pokerly.user.api;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.user.application.UserService;
import com.rolling.pokerly.user.dto.LoginRequest;
import com.rolling.pokerly.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager; // SecurityConfig에서 주입
    private final UserService userService;

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getNickname(),
                        request.getPassword()
                )
        );
        var u = userService.loadUser(request.getNickname());
        return UserResponse.from(u); // JWT 단계에서 Access/Refresh 반환으로 변경
    }
}
