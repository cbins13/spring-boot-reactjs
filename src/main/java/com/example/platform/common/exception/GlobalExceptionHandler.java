package com.example.platform.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Cross-cutting API error model. Keeping this in common/ means every future module/service
 * can share consistent error responses when extracted.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        details.put("fieldErrors", fieldErrors);
        
        log.warn("Validation error on {} {}: {}", 
                request.getMethod(), request.getRequestURI(), fieldErrors);
        
        return ResponseEntity.badRequest().body(baseError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, details));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseError(HttpStatus.FORBIDDEN, ex.getMessage(), request, null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        // All 401 responses go through here so clients always receive a consistent JSON body.
        // The `details` map can be used by the frontend to highlight where/why auth failed.
        Map<String, Object> details = new HashMap<>();
        details.put("authError", ex.getClass().getSimpleName());
        details.put("path", request.getRequestURI());
        
        log.error("Authentication failed for {} {}: {} ({})", 
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(baseError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request on {} {}: {}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.badRequest().body(baseError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        log.error("Unhandled exception on {} {} - TraceId: {} - Exception: {}", 
                request.getMethod(), request.getRequestURI(), traceId, ex.getClass().getSimpleName(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, Map.of("traceId", traceId)));
    }


    private ApiError baseError(HttpStatus status, String message, HttpServletRequest request, Map<String, Object> details) {
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .traceId(null)
                .details(details)
                .build();
    }
}

