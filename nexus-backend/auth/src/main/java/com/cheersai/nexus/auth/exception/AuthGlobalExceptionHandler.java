package com.cheersai.nexus.auth.exception;

import com.cheersai.nexus.common.model.base.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(AuthBusinessException.class)
    public Result<Void> handleAuthBusinessException(AuthBusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = "参数错误";
        FieldError firstError = ex.getBindingResult().getFieldError();
        if (firstError != null && firstError.getDefaultMessage() != null) {
            message = firstError.getDefaultMessage();
        } else if (ex.getBindingResult().getGlobalError() != null
                && ex.getBindingResult().getGlobalError().getDefaultMessage() != null) {
            message = ex.getBindingResult().getGlobalError().getDefaultMessage();
        }
        return Result.error(AuthErrorCode.INVALID_PARAMETER.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        String message = "参数错误";
        FieldError firstError = ex.getBindingResult().getFieldError();
        if (firstError != null && firstError.getDefaultMessage() != null) {
            message = firstError.getDefaultMessage();
        }
        return Result.error(AuthErrorCode.INVALID_PARAMETER.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.error(AuthErrorCode.INVALID_PARAMETER.getCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ex.printStackTrace();
        return Result.error(AuthErrorCode.INVALID_PARAMETER.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled auth exception", ex);
        return Result.error(AuthErrorCode.INTERNAL_ERROR.getCode(), AuthErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }
}
