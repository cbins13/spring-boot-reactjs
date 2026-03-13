package com.example.platform.security.jwt;

import com.example.platform.modules.users.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-expiration-seconds}")
    private long accessExpirationSeconds;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        // HMAC-SHA key; ensure sufficiently long secret.
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(JwtClaims claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpirationSeconds);
        return Jwts.builder()
                .subject(Long.toString(claims.getUserId()))
                .claim("email", claims.getEmail())
                .claim("role", claims.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Instant getAccessTokenExpiryInstant() {
        return Instant.now().plusSeconds(accessExpirationSeconds);
    }

    public JwtClaims parseAndValidate(String token) {
        Claims body = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long userId = Long.parseLong(body.getSubject());
        String email = body.get("email", String.class);
        String roleStr = body.get("role", String.class);
        Role role = Role.valueOf(roleStr);
        return JwtClaims.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }
}

