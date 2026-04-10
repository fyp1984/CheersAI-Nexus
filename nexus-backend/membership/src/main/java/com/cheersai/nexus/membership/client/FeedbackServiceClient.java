package com.cheersai.nexus.membership.client;

import com.cheersai.nexus.common.model.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Feedback 服务 Feign Client
 * 用于跨服务查询外部反馈统计
 */
@FeignClient(
        name = "feedback",
        url = "${feedback.service.url:http://localhost:8083}",
        configuration = FeedbackClientConfig.class
)
public interface FeedbackServiceClient {

    @GetMapping("/api/v1/internal/feedback-stats/count-external-since")
    Result<Long> countExternalSince(@RequestParam("since") LocalDateTime since);
}
