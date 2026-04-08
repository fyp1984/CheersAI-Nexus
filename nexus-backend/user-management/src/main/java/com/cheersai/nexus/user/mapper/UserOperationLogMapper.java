package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.user.entity.UserOperationLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户操作日志 Mapper
 */
@Mapper
public interface UserOperationLogMapper extends BaseMapper<UserOperationLog> {
}
