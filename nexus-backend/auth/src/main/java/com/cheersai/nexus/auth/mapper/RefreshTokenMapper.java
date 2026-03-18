package com.cheersai.nexus.auth.mapper;

import com.cheersai.nexus.auth.entity.RefreshToken;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
