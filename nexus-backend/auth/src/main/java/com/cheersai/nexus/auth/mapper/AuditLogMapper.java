package com.cheersai.nexus.auth.mapper;

import com.cheersai.nexus.auth.entity.AuditLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
