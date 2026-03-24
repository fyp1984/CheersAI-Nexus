package com.cheersai.nexus.auth.exception;

import lombok.Getter;

@Getter
public class AuthBusinessException extends RuntimeException {

    private final int code;

    public AuthBusinessException(AuthErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public AuthBusinessException(AuthErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
