package com.example.platform.modules.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_actor_user_id", columnList = "actor_user_id"),
        @Index(name = "idx_audit_logs_action_type", columnList = "action_type"),
        @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "target_entity", nullable = false, length = 100)
    private String targetEntity;

    @Column(name = "target_entity_id", length = 100)
    private String targetEntityId;

    @Lob
    private String details;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

