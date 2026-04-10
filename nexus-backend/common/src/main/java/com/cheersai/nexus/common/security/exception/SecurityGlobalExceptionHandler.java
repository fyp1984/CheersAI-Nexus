package com.cheersai.nexus.common.security.exception;

import com.cheersai.nexus.common.model.base.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 安全相关全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class SecurityGlobalExceptionHandler {

    @ExceptionHandler(ApiKeyAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleApiKeyAuth(ApiKeyAuthenticationException e) {
        log.warn("API Key认证失败: {}", e.getMessage());
        return Result.error(401, e.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimit(RateLimitExceededException e) {
        log.warn("请求频率超限: {}", e.getMessage());
        return Result.error(429, e.getMessage());
    }

    @ExceptionHandler(DesktopMemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleMemberNotFound(DesktopMemberNotFoundException e) {
        return Result.error(404, e.getMessage());
    }
}
