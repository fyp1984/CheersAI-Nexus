package com.cheersai.nexus.membership.mapper;

import com.cheersai.nexus.membership.entity.MembershipPlan;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员计划 Mapper 接口
 */
@Mapper
public interface MembershipPlanMapper extends BaseMapper<MembershipPlan> {
}
