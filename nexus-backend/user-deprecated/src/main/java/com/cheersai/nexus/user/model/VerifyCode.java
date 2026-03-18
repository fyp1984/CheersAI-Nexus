package com.cheersai.nexus.user.model;


import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

import static com.mybatisflex.annotation.KeyType.Auto;

/**
 * 验证码实体
 */
@Data
@Table("sys_verify_code")
public class VerifyCode {

    /**
     * ID
     */
    @Id(keyType = Auto)
    private Long id;

    /**
     * 验证码目标(邮箱/手机号)
     */
    private String target;

    /**
     * 验证码
     */
    private String code;

    /**
     * 类型(email/phone)
     */
    private String type;

    /**
     * 用途(register/reset_password/login)
     */
    private String purpose;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
