package com.cheersai.nexus.user.exception;

import lombok.Getter;

@Getter
public class UserBusinessException extends RuntimeException {

    private final int code;

    public UserBusinessException(UserErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public UserBusinessException(UserErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

