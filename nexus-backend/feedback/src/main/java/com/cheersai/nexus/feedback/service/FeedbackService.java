package com.cheersai.nexus.feedback.service;

import com.cheersai.nexus.feedback.entity.Feedback;

import java.util.List;
import java.util.UUID;

/**
 * 用户反馈业务逻辑接口
 */
public interface FeedbackService{

    /**
     * 根据 ID 获取反馈详情
     */
    Feedback getById(UUID id);

    /**
     * 获取反馈列表（支持筛选，全量返回供前端分页）
     */
    List<Feedback> getFeedbackList(String product, String type, String status);

    /**
     * 更新反馈状态和优先级
     */
    void updateFeedback(UUID id, String status, String priority);

    /**
     * 分配处理人
     */
    void assignFeedback(UUID id, UUID assigneeId);
}
