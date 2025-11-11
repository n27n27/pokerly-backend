package com.rolling.pokerly.user.dto;

import com.rolling.pokerly.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String nickname;
    private String role;
    private boolean enabled;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .role(u.getRole())
                .enabled(u.isEnabled())
                .build();
    }
}
