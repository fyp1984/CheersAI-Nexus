package com.cheersai.nexus.user.mapper;

import com.cheersai.nexus.user.model.VerifyCode;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码Mapper
 */
@Mapper
public interface VerifyCodeMapper extends BaseMapper<VerifyCode> {
}
