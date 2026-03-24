package com.cheersai.nexus.feedback.exception;

import lombok.Getter;

@Getter
public class FeedbackBusinessException extends RuntimeException {

    private final int code;

    public FeedbackBusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static FeedbackBusinessException notFound() {
        return new FeedbackBusinessException(40401, "反馈不存在");
    }

    public static FeedbackBusinessException invalidParameter(String message) {
        return new FeedbackBusinessException(40001, message);
    }
}
