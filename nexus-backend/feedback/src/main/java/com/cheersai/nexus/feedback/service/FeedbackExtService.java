package com.cheersai.nexus.feedback.service;

import com.cheersai.nexus.feedback.dto.request.FeedbackSubmitRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 外部反馈服务（来自 Desktop 客户端的反馈）
 */
public interface FeedbackExtService {

    /**
     * 提交外部反馈
     */
    UUID submitFeedback(FeedbackSubmitRequest request);

    /**
     * 统计指定时间以来的外部反馈数量
     */
    long countExternalSince(LocalDateTime since);
}
