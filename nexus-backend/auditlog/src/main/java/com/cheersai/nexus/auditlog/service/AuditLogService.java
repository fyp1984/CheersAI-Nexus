package com.cheersai.nexus.auditlog.service;


import com.cheersai.nexus.auditlog.dto.AuditLogDTO;
import com.cheersai.nexus.auditlog.dto.AuditLogQueryDTO;
import com.cheersai.nexus.auditlog.entity.AuditLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 审计日志业务逻辑接口
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 查询审计日志列表
     */
    Map<String, Object> queryAuditLogs(AuditLogQueryDTO queryDTO);

    /**
     * 获取审计日志详情
     */
    AuditLogDTO getAuditLogById(String id);

    /**
     * 记录审计日志
     */
    void recordAuditLog(AuditLogDTO auditLogDTO) throws JsonProcessingException;

    /**
     * 导出审计日志
     */
    List<AuditLogDTO> exportAuditLogs(AuditLogQueryDTO queryDTO);

    /**
     * 清理过期日志（根据保留期限）
     */
    int cleanExpiredLogs();
}
