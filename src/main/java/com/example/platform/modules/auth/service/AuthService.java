package com.example.platform.modules.auth.service;

import com.example.platform.modules.auth.dto.AuthResponse;
import com.example.platform.modules.auth.dto.LoginRequest;
import com.example.platform.modules.auth.dto.RefreshRequest;
import com.example.platform.modules.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    /**
     * Best-effort logout. If the refresh token is present, it will be revoked server-side.
     * Cookie clearing is handled at the controller layer.
     */
    void logout(String refreshToken);
}

