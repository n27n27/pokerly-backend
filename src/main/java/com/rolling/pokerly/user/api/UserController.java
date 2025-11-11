package com.rolling.pokerly.user.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rolling.pokerly.user.application.UserService;
import com.rolling.pokerly.user.dto.RegisterRequest;
import com.rolling.pokerly.user.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return userService.register(request);
    }

    // JWT 붙인 뒤에는 /me 로 대체 예정(현재 임시)
    @GetMapping("/{nickname}")
    public UserResponse getByNickname(@PathVariable("nickname") String nickname) {
        var u = userService.loadUser(nickname);
        return UserResponse.from(u);
    }

}
