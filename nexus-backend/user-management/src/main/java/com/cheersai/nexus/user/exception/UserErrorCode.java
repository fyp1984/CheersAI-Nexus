package com.cheersai.nexus.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {
    INVALID_PARAMETER(40001, "参数错误"),
    USER_NOT_FOUND(40401, "用户不存在"),
    USERNAME_EXISTS(40901, "用户名已存在"),
    EMAIL_EXISTS(40902, "邮箱已存在"),
    PHONE_EXISTS(40903, "手机号已存在"),
    INVALID_STATUS(40002, "用户状态不合法"),
    INVALID_ROLE(40003, "用户角色不合法"),
    INTERNAL_ERROR(50000, "系统开小差了，请稍后重试");

    private final int code;
    private final String defaultMessage;
}

