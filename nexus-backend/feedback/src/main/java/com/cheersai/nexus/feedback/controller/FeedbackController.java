package com.cheersai.nexus.feedback.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.feedback.dto.FeedbackAssignDTO;
import com.cheersai.nexus.feedback.dto.FeedbackUpdateDTO;
import com.cheersai.nexus.feedback.entity.Feedback;
import com.cheersai.nexus.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 用户反馈管理控制器
 */
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 获取反馈列表
     * (全量返回，由前端进行客户端分页)
     */
    @GetMapping
    public Result<List<Feedback>> getFeedbackList(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.success(feedbackService.getFeedbackList(product, type, status));
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{id}")
    public Result<Feedback> getFeedbackDetail(@PathVariable("id") UUID id) {
        return Result.success(feedbackService.getById(id));
    }

    /**
     * 更新反馈状态/优先级
     */
    @PutMapping("/{id}")
    public Result<Void> updateFeedback(@PathVariable UUID id, @RequestBody FeedbackUpdateDTO dto) {
        feedbackService.updateFeedback(id, dto.getStatus(), dto.getPriority());
        return Result.success();
    }

    /**
     * 分配处理人
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assignFeedback(@PathVariable UUID id, @RequestBody FeedbackAssignDTO dto) {
        feedbackService.assignFeedback(id, dto.getAssigneeId());
        return Result.success();
    }
}
