package com.cheersai.nexus.auditlog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    /**
     * 日志ID
     */
    private String id;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * 日志类型描述
     */
    private String logTypeDesc;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 操作类型描述
     */
    private String actionDesc;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 操作前数据
     */
    private Object beforeData;

    /**
     * 操作后数据
     */
    private Object afterData;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作结果
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
