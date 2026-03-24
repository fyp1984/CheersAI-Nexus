package com.cheersai.nexus.auth.exception;

import lombok.Getter;

@Getter
public enum AuthErrorCode {

    INVALID_PARAMETER(40001, "参数错误"),
    INVALID_PHONE_FORMAT(40011, "手机号格式不正确"),
    INVALID_VERIFICATION_CODE(40021, "验证码错误或已过期"),
    LOGIN_FAILED(40101, "用户名或密码错误"),
    ACCOUNT_DISABLED(40102, "账户已被禁用"),
    INVALID_REFRESH_TOKEN(40103, "无效的刷新令牌"),
    REFRESH_TOKEN_EXPIRED(40104, "刷新令牌已失效"),
    USER_NOT_FOUND(40401, "用户不存在"),
    EMAIL_ALREADY_EXISTS(40901, "该邮箱已被注册"),
    PHONE_ALREADY_EXISTS(40902, "该手机号已被注册"),
    USERNAME_ALREADY_EXISTS(40903, "该用户名已存在"),
    VERIFICATION_CODE_RATE_LIMIT(42901, "验证码发送过于频繁，请稍后再试"),
    INTERNAL_ERROR(50000, "系统开小差了，请稍后重试");

    private final int code;
    private final String defaultMessage;

    AuthErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
