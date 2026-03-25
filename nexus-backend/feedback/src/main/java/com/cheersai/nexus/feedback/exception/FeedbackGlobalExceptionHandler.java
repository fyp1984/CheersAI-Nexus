package com.cheersai.nexus.feedback.exception;

import com.cheersai.nexus.common.model.base.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.cheersai.nexus.feedback")
public class FeedbackGlobalExceptionHandler {

    @ExceptionHandler(FeedbackBusinessException.class)
    public Result<Void> handleBusinessException(FeedbackBusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException() {
        return Result.error(40001, "请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled feedback exception", ex);
        return Result.error(50000, "系统开小差了，请稍后重试");
    }
}
