package com.example.platform.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateUserRequest {
    @Email
    String email;

    @Size(min = 8, max = 72)
    String password;
}

