package com.example.platform.modules.auth.service;

import java.time.Instant;
import com.example.platform.modules.auth.dto.AuthResponse;
import com.example.platform.modules.auth.dto.LoginRequest;
import com.example.platform.modules.auth.dto.RefreshRequest;
import com.example.platform.modules.auth.dto.RegisterRequest;
import com.example.platform.modules.auth.entity.RefreshTokenEntity;
import com.example.platform.modules.auth.repository.RefreshTokenRepository;
import com.example.platform.modules.users.dto.CreateUserRequest;
import com.example.platform.modules.users.entity.Role;
import com.example.platform.modules.users.entity.RoleEntity;
import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.service.UsersService;
import com.example.platform.modules.audit.service.AuditService;
import com.example.platform.security.jwt.JwtClaims;
import com.example.platform.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersService usersService; // module boundary: depend on interface only
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final AuditService auditService;

    @Value("${app.jwt.access-expiration-seconds}")
    private long accessExpSeconds;

    @Value("${app.jwt.refresh-expiration-seconds}")
    private long refreshExpSeconds;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            // Users module owns persistence; auth module only invokes the users service boundary.
            var created = usersService.createUser(
                    new CreateUserRequest(request.getEmail(), request.getPassword(), null),
                    Role.ROLE_USER,
                    null);
            UserEntity entity = usersService.findEntityByEmail(created.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User just created but not found"));

            // Self-registration: record audit with actor = new user.
            auditService.logUserCreated(entity.getId(), entity.getId(), entity.getEmail());
            
            return issueTokens(entity);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("User registration failed for {} after {}ms: {}", request.getEmail(), duration, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            UserEntity user = usersService.findEntityByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("Login failed - user not found: {} after {}ms", request.getEmail(), duration);
                        return new IllegalArgumentException("Invalid credentials");
                    });

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Login failed - invalid password for: {} after {}ms", request.getEmail(), duration);
                throw new IllegalArgumentException("Invalid credentials");
            }
            
            return issueTokens(user);
        } catch (IllegalArgumentException e) {
            // Already logged above
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Unexpected error during login for email: {} after {}ms", request.getEmail(), duration, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String raw = request.getRefreshToken();
            String tokenHash = refreshTokenHasher.sha256Hex(raw);
            var existingOpt = refreshTokenRepository.findByTokenHash(tokenHash);
           
            RefreshTokenEntity existing = existingOpt.orElseThrow(() -> {
                log.error("Token refresh failed - invalid refresh token");
                return new IllegalArgumentException("Invalid refresh token");
            });

            if (existing.getRevokedAt() != null) {
                log.error("Token refresh failed - token revoked at {}", existing.getRevokedAt());
                throw new IllegalArgumentException("Refresh token revoked");
            }
            if (existing.getExpiresAt().isBefore(Instant.now())) {
                log.error("Token refresh failed - token expired at {}", existing.getExpiresAt());
                throw new IllegalArgumentException("Refresh token expired");
            }

            UserEntity user = usersService.findEntityById(existing.getUserId())
                    .orElseThrow(() -> {
                        log.error("User not found for refresh token, userId: {}", existing.getUserId());
                        return new IllegalArgumentException("User not found");
                    });

            // IMPORTANT: Do NOT rotate refresh tokens here unless the controller also updates the HttpOnly cookie.
            // Rotating without resetting the cookie will immediately break session persistence on the next reload.
            return issueAccessToken(user);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Token refresh failed after {}ms: {}", duration, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            String tokenHash = refreshTokenHasher.sha256Hex(refreshToken);
            var existingOpt = refreshTokenRepository.findByTokenHash(tokenHash);
            if (existingOpt.isEmpty()) {
                return;
            }

            RefreshTokenEntity existing = existingOpt.get();
            if (existing.getRevokedAt() != null) {
                return;
            }

            existing.setRevokedAt(Instant.now());
            refreshTokenRepository.save(existing);
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw e;
        }
    }

    private java.util.List<String> extractRoles(UserEntity user) {
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .toList();
    }

    private AuthResponse issueAccessToken(UserEntity user) {
        String access = jwtTokenProvider.generateAccessToken(JwtClaims.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(extractRoles(user))
                .build());

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(null)
                .accessExpiresInSeconds(accessExpSeconds)
                .refreshExpiresInSeconds(refreshExpSeconds)
                .build();
    }

    private AuthResponse issueTokens(UserEntity user) {
        String access = jwtTokenProvider.generateAccessToken(JwtClaims.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(extractRoles(user))
                .build());

        String refreshRaw = refreshTokenGenerator.generate();
        String refreshHash = refreshTokenHasher.sha256Hex(refreshRaw);
        Instant refreshExp = Instant.now().plusSeconds(refreshExpSeconds);

        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(user.getId())
                .tokenHash(refreshHash)
                .expiresAt(refreshExp)
                .revokedAt(null)
                .build());

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refreshRaw)
                .accessExpiresInSeconds(accessExpSeconds)
                .refreshExpiresInSeconds(refreshExpSeconds)
                .build();
    }
}

