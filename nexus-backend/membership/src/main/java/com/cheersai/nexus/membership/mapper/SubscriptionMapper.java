package com.cheersai.nexus.membership.mapper;

import com.cheersai.nexus.membership.entity.Subscription;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户订阅 Mapper 接口
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
}
