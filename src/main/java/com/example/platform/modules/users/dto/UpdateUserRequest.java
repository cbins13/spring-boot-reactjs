package com.example.platform.modules.users.dto;

import com.example.platform.modules.users.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateUserRequest {
    @Email
    String email;

    @Size(min = 8, max = 72)
    String password;

    /**
     * Optional role update. Only admins should be allowed to change user roles;
     * method-level security on the controller enforces this.
     */
    Role role;
}

