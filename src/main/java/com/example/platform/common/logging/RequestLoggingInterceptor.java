package com.example.platform.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Interceptor that logs HTTP requests and responses with timing information
 * and user context while filtering sensitive data.
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final String TRACE_ID = "traceId";
    
    // Headers to exclude from logging for security
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList(
            "authorization", "cookie", "set-cookie", "x-auth-token", "x-api-key"
    ));
    
    // Paths to exclude from detailed logging
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/actuator", "/health", "/metrics", "/favicon.ico"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        String traceId = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        request.setAttribute(REQUEST_START_TIME, startTime);
        request.setAttribute(TRACE_ID, traceId);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // This method is called after the handler method but before the view is rendered
        // We can add additional logging here if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        String traceId = (String) request.getAttribute(TRACE_ID);
        
        if (startTime == null || traceId == null) {
            return;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        String userEmail = getCurrentUserEmail();
        
        // Skip detailed logging for excluded paths
        if (shouldSkipLogging(request.getRequestURI())) {
            return;
        }
        
        // Only log errors, failures, and slow requests
        if (ex != null) {
            log.error("[TRACE:{}] Request failed: {} {} - Status: {} - Duration: {}ms - User: {} - Exception: {}", 
                    traceId, request.getMethod(), request.getRequestURI(), response.getStatus(), duration, 
                    userEmail != null ? userEmail : "anonymous", ex.getMessage());
        } else if (response.getStatus() >= 500) {
            log.error("[TRACE:{}] Request error: {} {} - Status: {} - Duration: {}ms - User: {}", 
                    traceId, request.getMethod(), request.getRequestURI(), response.getStatus(), duration, 
                    userEmail != null ? userEmail : "anonymous");
        } else if (response.getStatus() >= 400) {
            log.warn("[TRACE:{}] Request error: {} {} - Status: {} - Duration: {}ms - User: {}", 
                    traceId, request.getMethod(), request.getRequestURI(), response.getStatus(), duration, 
                    userEmail != null ? userEmail : "anonymous");
        } else if (duration > 5000) { // Only log slow successful requests
            log.warn("Slow request: {} {} took {}ms - User: {}", 
                    request.getMethod(), request.getRequestURI(), duration, 
                    userEmail != null ? userEmail : "anonymous");
        }
    }

    // Verbose logging methods removed to save log space

    private boolean shouldSkipLogging(String uri) {
        return EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean isSensitiveParameter(String paramName) {
        String lowerParam = paramName.toLowerCase();
        return lowerParam.contains("password") || 
               lowerParam.contains("token") || 
               lowerParam.contains("secret") || 
               lowerParam.contains("key") ||
               lowerParam.contains("auth");
    }

    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore exceptions when getting user context
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}