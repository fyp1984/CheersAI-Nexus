package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.user.entity.UserBehaviorTracking;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为追踪 Mapper
 */
@Mapper
public interface UserBehaviorTrackingMapper extends BaseMapper<UserBehaviorTracking> {
}
