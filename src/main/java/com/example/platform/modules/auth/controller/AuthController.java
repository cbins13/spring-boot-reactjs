package com.example.platform.modules.auth.controller;

import com.example.platform.modules.auth.dto.AuthResponse;
import com.example.platform.modules.auth.dto.LoginRequest;
import com.example.platform.modules.auth.dto.RefreshRequest;
import com.example.platform.modules.auth.dto.RegisterRequest;
import com.example.platform.modules.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = "/";
    // Keep defaults explicit so login + logout always match.
    private static final String REFRESH_COOKIE_SAMESITE = "Lax";

    private static ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(false) // TODO: set true when using HTTPS
                .path(REFRESH_COOKIE_PATH)
                .sameSite(REFRESH_COOKIE_SAMESITE)
                .maxAge(maxAgeSeconds)
                .build();
    }

    private static ResponseCookie buildRefreshCookieClearing(String path, boolean secure) {
        // Important: cookie removal must match name + path (+ domain if you set one).
        // Some browsers will ignore Set-Cookie with Secure=true over HTTP, so we emit
        // both variants in logout.
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .sameSite(REFRESH_COOKIE_SAMESITE)
                .maxAge(0)
                .build();
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse tokens = authService.login(request);

        // Issue HttpOnly refresh token cookie for the SPA; keep token out of JS.
        ResponseCookie cookie = buildRefreshCookie(tokens.getRefreshToken(), tokens.getRefreshExpiresInSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Frontend only needs the access token; refresh token stays in cookie.
        return AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(null)
                .accessExpiresInSeconds(tokens.getAccessExpiresInSeconds())
                .refreshExpiresInSeconds(tokens.getRefreshExpiresInSeconds())
                .build();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request) {
        // Extract refresh token from HttpOnly cookie (no request body expected).
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (REFRESH_COOKIE_NAME.equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Missing refresh token cookie");
        }
        RefreshRequest dto = new RefreshRequest(refreshToken);
        return authService.refresh(dto);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        // Best-effort revoke in DB (prevents re-auth even if cookie isn't cleared
        // client-side).
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {

            for (Cookie c : cookies) {

                if (REFRESH_COOKIE_NAME.equals(c.getName())) {
                    refreshToken = c.getValue();

                    break;
                }
            }
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                authService.logout(refreshToken);
            } catch (Exception e) {
                log.warn("Failed to revoke refresh token during logout: {}", e.getMessage());
            }
        }

        // Clear refresh token cookie for all likely paths (handles legacy cookies set
        // before path=/ was used).
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/", false).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/", true).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/api", false).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/api", true).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/api/auth", false).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieClearing("/api/auth", true).toString());
    }
}
