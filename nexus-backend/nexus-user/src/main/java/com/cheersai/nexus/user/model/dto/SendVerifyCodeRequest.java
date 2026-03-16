package com.cheersai.nexus.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendVerifyCodeRequest {

    /**
     * 目标（邮箱/手机号）
     */
    @NotBlank(message = "目标不能为空")
    private String target;

    /**
     * 类型（email/phone）
     */
    @NotBlank(message = "类型不能为空")
    private String type;

    /**
     * 用途（register/reset_password/login）
     */
    @NotBlank(message = "用途不能为空")
    private String purpose;
}
