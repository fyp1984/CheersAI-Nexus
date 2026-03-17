package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.model.usermanagement.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
