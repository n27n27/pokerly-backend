package com.rolling.pokerly.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {
    private String nickname;
    private String refreshToken;
}
