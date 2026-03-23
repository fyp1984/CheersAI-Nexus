package com.cheersai.nexus.auditlog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志查询DTO
 */
@Data
public class AuditLogQueryDTO {

    /**
     * 日志类型：user_action/admin_action/system_event/security_event
     */
    private String logType;

    /**
     * 操作类型（模糊匹配）
     */
    private String action;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称（模糊匹配）
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
     * IP地址
     */
    private String ipAddress;

    /**
     * 操作结果：success/failure
     */
    private String result;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}
