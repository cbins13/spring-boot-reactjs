package com.example.platform.modules.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String refreshToken;
    long accessExpiresInSeconds;
    long refreshExpiresInSeconds;
}

