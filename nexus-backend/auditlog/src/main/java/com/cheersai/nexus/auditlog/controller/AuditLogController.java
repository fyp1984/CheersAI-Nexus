package com.cheersai.nexus.auditlog.controller;

import com.cheersai.nexus.auditlog.dto.AuditLogDTO;
import com.cheersai.nexus.auditlog.dto.AuditLogQueryDTO;
import com.cheersai.nexus.auditlog.service.AuditLogService;
import com.cheersai.nexus.common.model.base.Result;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    @Autowired
    private final AuditLogService auditLogService;

    /**
     * 查询审计日志列表
     */
    @GetMapping
    public Result<Map<String, Object>> queryAuditLogs(
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setLogType(logType);
        queryDTO.setAction(action);
        queryDTO.setOperatorId(operatorId);
        queryDTO.setOperatorName(operatorName);
        queryDTO.setTargetType(targetType);
        queryDTO.setTargetId(targetId);
        queryDTO.setIpAddress(ipAddress);
        queryDTO.setResult(result);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);

        return Result.success(auditLogService.queryAuditLogs(queryDTO));
    }

    /**
     * 获取审计日志详情
     */
    @GetMapping("/{id}")
    public Result<AuditLogDTO> getAuditLogDetail(@PathVariable("id") String id) {
        AuditLogDTO auditLog = auditLogService.getAuditLogById(id);
        if (auditLog == null) {
            return Result.error("日志不存在");
        }
        return Result.success(auditLog);
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    public Result<List<AuditLogDTO>> exportAuditLogs(
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setLogType(logType);
        queryDTO.setAction(action);
        queryDTO.setOperatorId(operatorId);
        queryDTO.setOperatorName(operatorName);
        queryDTO.setTargetType(targetType);
        queryDTO.setTargetId(targetId);
        queryDTO.setIpAddress(ipAddress);
        queryDTO.setResult(result);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);

        return Result.success(auditLogService.exportAuditLogs(queryDTO));
    }

    /**
     * 清理过期日志（仅管理员可调用）
     */
    @DeleteMapping("/cleanup")
    public Result<Integer> cleanExpiredLogs() {
        return Result.success(auditLogService.cleanExpiredLogs());
    }
}
