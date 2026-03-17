package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.user.model.RefreshToken;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌Mapper
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
