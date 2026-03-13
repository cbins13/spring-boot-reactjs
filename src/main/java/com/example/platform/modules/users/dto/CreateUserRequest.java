package com.example.platform.modules.users.dto;

import com.example.platform.modules.users.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateUserRequest {
    @Email
    @NotBlank
    String email;

    @NotBlank
    @Size(min = 8, max = 72)
    String password;

    /**
     * Optional role to assign to the new user. When null, the service layer will
     * fall back to a default (e.g. ROLE_USER). Keeping this here allows the admin
     * UI to explicitly create admins.
     */
    Role role;
}

