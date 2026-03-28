package com.cheersai.nexus.auth.entity;

import com.mybatisflex.annotation.Column;
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
@Table("nexus.audit_logs")
public class AuditLog {

    @Id
    private String id;

    private String logType;
    
    private String operatorId;

    private String operatorName;

    private String targetType;

    private String targetID;
    
    private String beforeData;
    
    private String afterData;

    private String action;  // login, logout, register, password_reset, etc.

    private String ipAddress;

    private String userAgent;

    @Builder.Default
    private String result = "success";

    private String errorMessage;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
