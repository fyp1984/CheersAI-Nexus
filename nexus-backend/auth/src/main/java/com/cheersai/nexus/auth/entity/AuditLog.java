package com.cheersai.nexus.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_logs")
public class AuditLog {

    @Id
    private String id;

    private String userId;

    private String action;  // login, logout, register, password_reset, etc.

    private String ipAddress;

    private String userAgent;

    @Builder.Default
    private Boolean success = true;

    private String details; // JSON

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
