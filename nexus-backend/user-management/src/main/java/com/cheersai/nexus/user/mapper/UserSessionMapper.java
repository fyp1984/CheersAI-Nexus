package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.user.entity.UserSession;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户会话 Mapper
 */
@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {
}
