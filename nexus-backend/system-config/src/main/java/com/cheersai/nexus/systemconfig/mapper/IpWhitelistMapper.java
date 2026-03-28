package com.cheersai.nexus.systemconfig.mapper;

import com.cheersai.nexus.systemconfig.entity.IpWhitelist;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * IP白名单 Mapper
 */
@Mapper
public interface IpWhitelistMapper extends BaseMapper<IpWhitelist> {
}