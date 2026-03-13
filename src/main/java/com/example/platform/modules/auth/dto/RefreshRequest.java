package com.example.platform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshRequest {
    @NotBlank
    String refreshToken;
}

