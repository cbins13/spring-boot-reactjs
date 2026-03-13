package com.example.platform.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class RegisterRequest {
    @Email
    @NotBlank
    String email;

    @NotBlank
    @Size(min = 8, max = 72)
    String password;
}

