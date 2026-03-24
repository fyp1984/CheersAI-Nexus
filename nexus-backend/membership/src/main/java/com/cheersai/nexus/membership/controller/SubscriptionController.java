package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.dto.SubscriptionCreateDTO;
import com.cheersai.nexus.membership.dto.SubscriptionDetailDTO;
import com.cheersai.nexus.membership.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅管理控制器
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 获取订阅列表
     */
    @GetMapping
    public Result<List<SubscriptionDetailDTO>> getSubscriptionList(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {
        return Result.success(subscriptionService.getSubscriptionList(userId, status));
    }

    /**
     * 获取订阅详情
     */
    @GetMapping("/{id}")
    public Result<SubscriptionDetailDTO> getSubscriptionDetail(@PathVariable("id") String id) {
        SubscriptionDetailDTO subscription = subscriptionService.getSubscriptionById(id);
        if (subscription == null) {
            return Result.error("订阅不存在");
        }
        return Result.success(subscription);
    }

    /**
     * 创建订阅
     */
    @PostMapping
    public Result<SubscriptionDetailDTO> createSubscription(@RequestBody SubscriptionCreateDTO dto) {
        return Result.success(subscriptionService.createSubscription(dto));
    }

    /**
     * 更新订阅
     */
    @PutMapping("/{id}")
    public Result<Void> updateSubscription(@PathVariable("id") String id,
                                            @RequestBody SubscriptionCreateDTO dto) {
        subscriptionService.updateSubscription(id, dto);
        return Result.success();
    }
}
