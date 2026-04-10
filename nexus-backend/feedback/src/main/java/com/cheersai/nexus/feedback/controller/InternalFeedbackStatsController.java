package com.cheersai.nexus.feedback.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.feedback.service.FeedbackExtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 内部反馈统计端点（供其他微服务通过 Feign 调用）
 * 需要 X-Internal-Secret header 认证
 */
@RestController
@RequestMapping("/api/v1/internal/feedback-stats")
@RequiredArgsConstructor
public class InternalFeedbackStatsController {

    private final FeedbackExtService feedbackExtService;

    @GetMapping("/count-external-since")
    public Result<Long> countExternalSince(
            @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        long count = feedbackExtService.countExternalSince(since);
        return Result.success(count);
    }
}
