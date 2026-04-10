package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.dto.request.EventBatchRequest;
import com.cheersai.nexus.membership.service.DesktopMemberEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部事件接收端点（需要 API Key 认证）
 * 由 Desktop 客户端调用，用于上报用户行为事件
 */
@RestController
@RequestMapping("/api/v1/external/events")
@RequiredArgsConstructor
public class ExternalEventController {

    private final DesktopMemberEventService eventService;

    @PostMapping
    public Result<Void> ingestEvents(@Valid @RequestBody EventBatchRequest request) {
        eventService.ingestEvents(request.getEvents());
        return Result.success();
    }
}
