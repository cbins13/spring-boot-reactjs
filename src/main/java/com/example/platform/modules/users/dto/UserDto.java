package com.example.platform.modules.users.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class UserDto {
    Long id;
    String email;
    List<String> roles;
    Instant createdAt;
    Instant updatedAt;
}

