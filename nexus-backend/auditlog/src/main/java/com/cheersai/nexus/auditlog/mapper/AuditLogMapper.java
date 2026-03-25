package com.cheersai.nexus.auditlog.mapper;

import com.cheersai.nexus.auditlog.entity.AuditLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper 接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
