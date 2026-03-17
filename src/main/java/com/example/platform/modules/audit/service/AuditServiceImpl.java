package com.example.platform.modules.audit.service;

import com.example.platform.modules.audit.entity.AuditLogEntity;
import com.example.platform.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    private void save(AuditLogEntity entity) {
        try {
            auditLogRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void logUserCreated(Long actorUserId, Long targetUserId, String email) {
        save(AuditLogEntity.builder()
                .actorUserId(actorUserId)
                .actionType("USER_CREATED")
                .targetEntity("User")
                .targetEntityId(targetUserId != null ? targetUserId.toString() : null)
                .details("{\"email\":\"" + email + "\"}")
                .build());
    }

    @Override
    @Transactional
    public void logUserUpdated(Long actorUserId, Long targetUserId, String oldEmail, String newEmail) {
        save(AuditLogEntity.builder()
                .actorUserId(actorUserId)
                .actionType("USER_UPDATED")
                .targetEntity("User")
                .targetEntityId(targetUserId != null ? targetUserId.toString() : null)
                .details("{\"oldEmail\":\"" + oldEmail + "\",\"newEmail\":\"" + newEmail + "\"}")
                .build());
    }

    @Override
    @Transactional
    public void logUserDeleted(Long actorUserId, Long targetUserId, String email) {
        save(AuditLogEntity.builder()
                .actorUserId(actorUserId)
                .actionType("USER_DELETED")
                .targetEntity("User")
                .targetEntityId(targetUserId != null ? targetUserId.toString() : null)
                .details("{\"email\":\"" + email + "\"}")
                .build());
    }

    @Override
    @Transactional
    public void logRoleChanged(Long actorUserId, Long targetUserId, List<String> oldRoles, List<String> newRoles, String ipAddress) {
        String details = "{\"oldRoles\":" + oldRoles + ",\"newRoles\":" + newRoles + "}";
        save(AuditLogEntity.builder()
                .actorUserId(actorUserId)
                .actionType("ROLE_CHANGED")
                .targetEntity("User")
                .targetEntityId(targetUserId != null ? targetUserId.toString() : null)
                .details(details)
                .ipAddress(ipAddress)
                .build());
    }

    @Override
    @Transactional
    public void logLoginSuccess(Long actorUserId, String ipAddress, String userAgent) {
        save(AuditLogEntity.builder()
                .actorUserId(actorUserId)
                .actionType("LOGIN_SUCCESS")
                .targetEntity("Auth")
                .details("{\"userAgent\":\"" + userAgent + "\"}")
                .ipAddress(ipAddress)
                .build());
    }

    @Override
    @Transactional
    public void logLoginFailure(String email, String ipAddress, String userAgent) {
        save(AuditLogEntity.builder()
                .actorUserId(null)
                .actionType("LOGIN_FAILURE")
                .targetEntity("Auth")
                .details("{\"email\":\"" + email + "\",\"userAgent\":\"" + userAgent + "\"}")
                .ipAddress(ipAddress)
                .build());
    }
}