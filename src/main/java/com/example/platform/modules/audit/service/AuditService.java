package com.example.platform.modules.audit.service;

import java.util.List;

public interface AuditService {
    void logUserCreated(Long actorUserId, Long targetUserId, String email);
    void logUserUpdated(Long actorUserId, Long targetUserId, String oldEmail, String newEmail);
    void logUserDeleted(Long actorUserId, Long targetUserId, String email);
    void logRoleChanged(Long actorUserId, Long targetUserId, List<String> oldRoles, List<String> newRoles, String ipAddress);
    void logLoginSuccess(Long actorUserId, String ipAddress, String userAgent);
    void logLoginFailure(String email, String ipAddress, String userAgent);
}