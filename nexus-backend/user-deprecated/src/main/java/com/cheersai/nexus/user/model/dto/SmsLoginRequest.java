package com.cheersai.nexus.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码登录请求DTO
 */
@Data
public class SmsLoginRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
