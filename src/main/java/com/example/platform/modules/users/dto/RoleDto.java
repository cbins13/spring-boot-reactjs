package com.example.platform.modules.users.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RoleDto {
    Long id;
    String name;
    String description;
    List<String> permissions;
}
