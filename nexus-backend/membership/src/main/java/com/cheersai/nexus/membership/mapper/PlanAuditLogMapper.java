package com.cheersai.nexus.membership.mapper;

import com.cheersai.nexus.membership.entity.PlanAuditLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员计划审批记录 Mapper 接口
 */
@Mapper
public interface PlanAuditLogMapper extends BaseMapper<PlanAuditLog> {
}
