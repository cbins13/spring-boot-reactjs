package com.example.platform.security.jwt;

import com.example.platform.modules.users.entity.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JwtClaims {
    long userId;
    String email;
    Role role;
}

