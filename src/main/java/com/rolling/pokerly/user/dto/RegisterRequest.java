package com.rolling.pokerly.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank @Size(max = 60)
    private String nickname;

    @NotBlank @Size(min = 6, max = 100)
    private String password;
}
