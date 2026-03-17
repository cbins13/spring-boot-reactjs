package com.example.platform.modules.users.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionDto {
    Long id;
    String name;
    String description;
}
