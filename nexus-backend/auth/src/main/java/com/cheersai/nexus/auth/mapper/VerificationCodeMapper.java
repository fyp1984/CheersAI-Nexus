package com.cheersai.nexus.auth.mapper;

import com.cheersai.nexus.auth.entity.VerificationCode;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {

    @Update("UPDATE verification_codes SET used = TRUE WHERE target = #{target} AND purpose = #{purpose} AND used = FALSE")
    int markUsedByTargetAndPurpose(@Param("target") String target, @Param("purpose") String purpose);
}
