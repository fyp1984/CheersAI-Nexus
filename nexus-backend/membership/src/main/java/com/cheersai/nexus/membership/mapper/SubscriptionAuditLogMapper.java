package com.cheersai.nexus.membership.mapper;

import com.cheersai.nexus.membership.entity.SubscriptionAuditLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户订阅变更审计日志 Mapper 接口
 */
@Mapper
public interface SubscriptionAuditLogMapper extends BaseMapper<SubscriptionAuditLog> {
}
