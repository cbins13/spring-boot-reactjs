package com.example.platform.modules.users.dto;

import com.example.platform.modules.users.entity.Role;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserDto {
    Long id;
    String email;
    Role role;
    Instant createdAt;
    Instant updatedAt;
}

