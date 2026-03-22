package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
