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
import com.example.platform.modules.users.entity.UserEntity;
import com.example.platform.modules.users.service.UsersService;
import com.example.platform.security.jwt.JwtClaims;
import com.example.platform.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsersService usersService; // module boundary: depend on interface only
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;

    @Value("${app.jwt.access-expiration-seconds}")
    private long accessExpSeconds;

    @Value("${app.jwt.refresh-expiration-seconds}")
    private long refreshExpSeconds;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Users module owns persistence; auth module only invokes the users service boundary.
        var created = usersService.createUser(new CreateUserRequest(request.getEmail(), request.getPassword()), Role.ROLE_USER);
        UserEntity entity = usersService.findEntityByEmail(created.getEmail())
                .orElseThrow(() -> new IllegalStateException("User just created but not found"));
        return issueTokens(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = usersService.findEntityByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        String raw = request.getRefreshToken();

        String tokenHash = refreshTokenHasher.sha256Hex(raw);
        var existingOpt = refreshTokenRepository.findByTokenHash(tokenHash);
       
        RefreshTokenEntity existing = existingOpt.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existing.getRevokedAt() != null) {
            throw new IllegalArgumentException("Refresh token revoked");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        UserEntity user = usersService.findEntityById(existing.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // IMPORTANT: Do NOT rotate refresh tokens here unless the controller also updates the HttpOnly cookie.
        // Rotating without resetting the cookie will immediately break session persistence on the next reload.
        return issueAccessToken(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            logger.debug("Logout called with null or blank refresh token");
            return;
        }

        String tokenHash = refreshTokenHasher.sha256Hex(refreshToken);
        var existingOpt = refreshTokenRepository.findByTokenHash(tokenHash);
        if (existingOpt.isEmpty()) {
            logger.debug("Refresh token not found in database (may have been already revoked or expired)");
            return;
        }

        RefreshTokenEntity existing = existingOpt.get();
        if (existing.getRevokedAt() != null) {
            logger.debug("Refresh token already revoked at {}", existing.getRevokedAt());
            return;
        }

        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);
        logger.info("Refresh token revoked for user ID: {}", existing.getUserId());
    }

    private AuthResponse issueAccessToken(UserEntity user) {
        String access = jwtTokenProvider.generateAccessToken(JwtClaims.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
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
                .role(user.getRole())
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

