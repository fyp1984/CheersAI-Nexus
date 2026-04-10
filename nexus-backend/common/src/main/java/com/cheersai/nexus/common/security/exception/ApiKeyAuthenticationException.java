package com.cheersai.nexus.common.security.exception;

public class ApiKeyAuthenticationException extends RuntimeException {
    public ApiKeyAuthenticationException(String message) {
        super(message);
    }
}
