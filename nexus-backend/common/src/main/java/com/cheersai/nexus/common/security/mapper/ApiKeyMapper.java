package com.cheersai.nexus.common.security.mapper;

import com.cheersai.nexus.common.security.entity.ApiKey;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {
}
