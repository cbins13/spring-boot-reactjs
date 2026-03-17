package com.example.platform.config;

import com.example.platform.common.exception.ApiError;
import com.example.platform.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        ObjectMapper objectMapper) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(eh -> eh
                                                // Ensure invalid/missing/expired JWT results in a JSON 401 body,
                                                // consistent with GlobalExceptionHandler.
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(UNAUTHORIZED.value());
                                                        response.setContentType("application/json");

                                                        String message = "Unauthorized - please log in again";

                                                        ApiError body = ApiError.builder()
                                                                        .timestamp(Instant.now())
                                                                        .status(UNAUTHORIZED.value())
                                                                        .error(UNAUTHORIZED.getReasonPhrase())
                                                                        .message(message)
                                                                        .path(request.getRequestURI())
                                                                        .traceId(null)
                                                                        .details(Map.of("authError",
                                                                                        authException.getClass()
                                                                                                        .getSimpleName()))
                                                                        .build();
                                                        try {
                                                                objectMapper.writeValue(response.getWriter(), body);
                                                        } catch (IOException e) {
                                                                // Fallback: at least send HTTP status if body write fails.
                                                                response.sendError(UNAUTHORIZED.value(),
                                                                                UNAUTHORIZED.getReasonPhrase());
                                                        }
                                                })
                                                // Authenticated but forbidden by RBAC/method security -> 403.
                                                .accessDeniedHandler((request, response,
                                                                accessDeniedException) -> response.sendError(
                                                                                FORBIDDEN.value(),
                                                                                FORBIDDEN.getReasonPhrase())))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.POST, "/api/auth/register",
                                                                "/api/auth/login", "/api/auth/refresh",
                                                                "/api/auth/logout")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}
