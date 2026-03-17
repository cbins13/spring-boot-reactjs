package com.example.platform.security.jwt;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JwtClaims {
    long userId;
    String email;
    java.util.List<String> roles;
}

