package com.cheersai.nexus.auditlog.service.impl;

import com.cheersai.nexus.auditlog.dto.AuditLogDTO;
import com.cheersai.nexus.auditlog.dto.AuditLogQueryDTO;
import com.cheersai.nexus.auditlog.entity.AuditLog;
import com.cheersai.nexus.auditlog.mapper.AuditLogMapper;
import com.cheersai.nexus.auditlog.service.AuditLogService;
import com.cheersai.nexus.common.utils.JacksonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cheersai.nexus.auditlog.entity.table.AuditLogTableDef.AUDIT_LOG;

/**
 * 审计日志业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Autowired
    private final JacksonUtils jacksonUtils;

    // 日志保留期限（天数）
    private static final Map<String, Integer> RETENTION_DAYS = Map.of(
            "user_action", 90,
            "admin_action", 365,
            "system_event", 30,
            "security_event", 365
    );

    @Override
    public Map<String, Object> queryAuditLogs(AuditLogQueryDTO queryDTO) {
        QueryWrapper queryWrapper = buildQueryWrapper(queryDTO);

        // 查询总数
        long total = this.count(queryWrapper);

        // 分页查询
        int offset = (queryDTO.getPage() - 1) * queryDTO.getPageSize();
        queryWrapper.limit(queryDTO.getPageSize(), offset);
        queryWrapper.orderBy(AUDIT_LOG.CREATED_AT.desc());

        List<AuditLog> logs = this.list(queryWrapper);
        List<AuditLogDTO> dtoList = logs.stream()
                .map(this::toAuditLogDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", dtoList);
        result.put("total", total);
        result.put("page", queryDTO.getPage());
        result.put("pageSize", queryDTO.getPageSize());

        return result;
    }

    @Override
    public AuditLogDTO getAuditLogById(String id) {
        AuditLog log = this.getById(id);
        if (log == null) {
            return null;
        }
        return toAuditLogDTO(log);
    }

    @Override
    @Transactional
    public void recordAuditLog(AuditLogDTO auditLogDTO) throws JsonProcessingException {
        AuditLog auditLog = AuditLog.builder()
                .logType(auditLogDTO.getLogType())
                .action(auditLogDTO.getAction())
                .operatorId(auditLogDTO.getOperatorId())
                .operatorName(auditLogDTO.getOperatorName())
                .targetType(auditLogDTO.getTargetType())
                .targetId(auditLogDTO.getTargetId())
                .beforeData(auditLogDTO.getBeforeData() != null ? jacksonUtils.toJson(auditLogDTO.getBeforeData()) : null)
                .afterData(auditLogDTO.getAfterData() != null ? jacksonUtils.toJson(auditLogDTO.getAfterData()) : null)
                .ipAddress(auditLogDTO.getIpAddress())
                .userAgent(auditLogDTO.getUserAgent())
                .result(auditLogDTO.getResult() != null ? auditLogDTO.getResult() : "success")
                .errorMessage(auditLogDTO.getErrorMessage())
                .createdAt(LocalDateTime.now())
                .build();

        this.save(auditLog);
    }

    @Override
    public List<AuditLogDTO> exportAuditLogs(AuditLogQueryDTO queryDTO) {
        QueryWrapper queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderBy(AUDIT_LOG.CREATED_AT.desc());

        // 导出最多10000条
        queryWrapper.limit(10000);

        List<AuditLog> logs = this.list(queryWrapper);
        return logs.stream()
                .map(this::toAuditLogDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int cleanExpiredLogs() {
        int deletedCount = 0;

        for (Map.Entry<String, Integer> entry : RETENTION_DAYS.entrySet()) {
            String logType = entry.getKey();
            int retentionDays = entry.getValue();

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

            QueryWrapper queryWrapper = QueryWrapper.create()
                    .from(AUDIT_LOG)
                    .where(AUDIT_LOG.LOG_TYPE.eq(logType))
                    .and(AUDIT_LOG.CREATED_AT.lt(cutoffDate));

            this.remove(queryWrapper);
        }

        log.info("Cleaned expired audit logs");
        return deletedCount;
    }

    private QueryWrapper buildQueryWrapper(AuditLogQueryDTO queryDTO) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(AUDIT_LOG);

        if (StringUtils.hasText(queryDTO.getLogType())) {
            queryWrapper.where(AUDIT_LOG.LOG_TYPE.eq(queryDTO.getLogType()));
        }

        if (StringUtils.hasText(queryDTO.getAction())) {
            queryWrapper.and(AUDIT_LOG.ACTION.like(queryDTO.getAction(), StringUtils.hasText(queryDTO.getAction())));
        }

        if (StringUtils.hasText(queryDTO.getOperatorId())) {
            queryWrapper.where(AUDIT_LOG.OPERATOR_ID.eq(queryDTO.getOperatorId()));
        }

        if (StringUtils.hasText(queryDTO.getOperatorName())) {
            queryWrapper.and(AUDIT_LOG.OPERATOR_NAME.like(queryDTO.getOperatorName(), StringUtils.hasText(queryDTO.getOperatorName())));
        }

        if (StringUtils.hasText(queryDTO.getTargetType())) {
            queryWrapper.where(AUDIT_LOG.TARGET_TYPE.eq(queryDTO.getTargetType()));
        }

        if (StringUtils.hasText(queryDTO.getTargetId())) {
            queryWrapper.where(AUDIT_LOG.TARGET_ID.eq(queryDTO.getTargetId()));
        }

        if (StringUtils.hasText(queryDTO.getIpAddress())) {
            queryWrapper.and(AUDIT_LOG.IP_ADDRESS.like(queryDTO.getIpAddress(), StringUtils.hasText(queryDTO.getIpAddress())));
        }

        if (StringUtils.hasText(queryDTO.getResult())) {
            queryWrapper.where(AUDIT_LOG.RESULT.eq(queryDTO.getResult()));
        }

        if (queryDTO.getStartTime() != null) {
            queryWrapper.and(AUDIT_LOG.CREATED_AT.ge(queryDTO.getStartTime()));
        }

        if (queryDTO.getEndTime() != null) {
            queryWrapper.and(AUDIT_LOG.CREATED_AT.le(queryDTO.getEndTime()));
        }

        return queryWrapper;
    }

    private AuditLogDTO toAuditLogDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .logType(log.getLogType())
                .logTypeDesc(getLogTypeDesc(log.getLogType()))
                .action(log.getAction())
                .actionDesc(getActionDesc(log.getAction()))
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .beforeData(parseJson(log.getBeforeData()))
                .afterData(parseJson(log.getAfterData()))
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .result(log.getResult())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String getLogTypeDesc(String logType) {
        return switch (logType) {
            case "user_action" -> "用户行为";
            case "admin_action" -> "管理操作";
            case "system_event" -> "系统事件";
            case "security_event" -> "安全事件";
            default -> logType;
        };
    }

    private String getActionDesc(String action) {
        // 常用操作描述映射
        return switch (action) {
            case "login" -> "用户登录";
            case "logout" -> "用户登出";
            case "register" -> "用户注册";
            case "create_plan" -> "创建会员计划";
            case "update_plan" -> "更新会员计划";
            case "delete_plan" -> "删除会员计划";
            case "audit_plan" -> "审批会员计划";
            case "create_subscription" -> "创建订阅";
            case "update_subscription" -> "更新订阅";
            case "adjust_subscription" -> "调整订阅";
            case "cancel_subscription" -> "取消订阅";
            default -> action;
        };
    }

    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return jacksonUtils.fromJson(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
