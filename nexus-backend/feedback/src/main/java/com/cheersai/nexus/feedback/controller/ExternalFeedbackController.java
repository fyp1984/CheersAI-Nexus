package com.cheersai.nexus.feedback.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.feedback.dto.request.FeedbackSubmitRequest;
import com.cheersai.nexus.feedback.service.FeedbackExtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * 外部反馈提交端点（需要 API Key 认证）
 * 由 Desktop 客户端调用
 */
@RestController
@RequestMapping("/api/v1/external/feedbacks")
@RequiredArgsConstructor
public class ExternalFeedbackController {

    private final FeedbackExtService feedbackExtService;

    @PostMapping
    public Result<Map<String, String>> submitFeedback(@Valid @RequestBody FeedbackSubmitRequest request) {
        UUID feedbackId = feedbackExtService.submitFeedback(request);
        return Result.success(Map.of("feedbackId", feedbackId.toString()));
    }
}
