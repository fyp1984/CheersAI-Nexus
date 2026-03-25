package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.dto.SubscriptionAdjustDTO;
import com.cheersai.nexus.membership.dto.UserSubscriptionDTO;
import com.cheersai.nexus.membership.entity.SubscriptionAuditLog;
import com.cheersai.nexus.membership.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户订阅管理控制器（手动调整用户会员）
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 获取用户当前会员信息
     */
    @GetMapping("/{userId}/subscription")
    public Result<UserSubscriptionDTO> getUserSubscription(@PathVariable("userId") String userId) {
        return Result.success(subscriptionService.getUserSubscription(userId));
    }

    /**
     * 手动调整用户会员（运营操作）
     */
    @PostMapping("/{userId}/subscription/adjust")
    public Result<UserSubscriptionDTO> adjustUserSubscription(
            @PathVariable("userId") String userId,
            @RequestBody SubscriptionAdjustDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) String operatorId,
            @RequestHeader(value = "X-User-Name", required = false) String operatorName,
            HttpServletRequest request) {
        
        String actualOperatorId = operatorId != null ? operatorId : "operator";
        String actualOperatorName = operatorName != null ? operatorName : "运营人员";
        String operatorIp = getClientIp(request);
        
        UserSubscriptionDTO result = subscriptionService.adjustUserSubscription(
                userId, dto, actualOperatorId, actualOperatorName, operatorIp);
        
        return Result.success(result);
    }

    /**
     * 获取用户订阅变更审计日志
     */
    @GetMapping("/{userId}/subscription/audit-logs")
    public Result<List<SubscriptionAuditLog>> getSubscriptionAuditLogs(@PathVariable("userId") String userId) {
        return Result.success(subscriptionService.getSubscriptionAuditLogs(userId));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
