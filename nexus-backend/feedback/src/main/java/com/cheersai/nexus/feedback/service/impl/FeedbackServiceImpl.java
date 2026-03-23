package com.cheersai.nexus.feedback.service.impl;

import com.cheersai.nexus.feedback.entity.Feedback;
import com.cheersai.nexus.feedback.mapper.FeedbackMapper;
import com.cheersai.nexus.feedback.service.FeedbackService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.cheersai.nexus.feedback.entity.table.FeedbackTableDef.FEEDBACK;

/**
 * 用户反馈业务逻辑实现类
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public List<Feedback> getFeedbackList(String product, String type, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(FEEDBACK)
                .where(FEEDBACK.PRODUCT_ID.eq(product, StringUtils.hasText(product)))
                .and(FEEDBACK.TYPE.eq(type, StringUtils.hasText(type)))
                .and(FEEDBACK.STATUS.eq(status, StringUtils.hasText(status)))
                .orderBy(FEEDBACK.CREATED_AT.desc());
        return feedbackMapper.selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional
    public void updateFeedback(UUID id, String status, String priority) {
        // 核心：只更新需要修改的字段，完全避开 attachments 等 JSONB 字段
        UpdateChain<Feedback> updateChain = UpdateChain.of(Feedback.class)
                .set(FEEDBACK.UPDATED_AT, LocalDateTime.now()); // 强制更新修改时间

        // 只更新非空的字段
        if (StringUtils.hasText(status)) {
            updateChain.set(FEEDBACK.STATUS, status);
        }
        if (StringUtils.hasText(priority)) {
            updateChain.set(FEEDBACK.PRIORITY, priority);
        }

        // 执行更新
        updateChain.where(FEEDBACK.ID.eq(id)).update();
    }

    @Override
    @Transactional
    public void assignFeedback(UUID id, UUID assigneeId) {
        UpdateChain.of(Feedback.class)
                .set(FEEDBACK.ASSIGNEE_ID, assigneeId)    // 只更处理人ID
                .set(FEEDBACK.UPDATED_AT, LocalDateTime.now()) // 只更修改时间
                .where(FEEDBACK.ID.eq(id))               // 条件：匹配反馈ID
                .update();                               // 执行更新
    }
}
