package com.cheersai.nexus.feedback.service.impl;

import com.cheersai.nexus.feedback.entity.Feedback;
import com.cheersai.nexus.feedback.exception.FeedbackBusinessException;
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
    public Feedback getById(UUID id) {
        return requireFeedback(id);
    }

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
        requireFeedback(id);

        UpdateChain<Feedback> updateChain = UpdateChain.of(Feedback.class)
                .set(FEEDBACK.UPDATED_AT, LocalDateTime.now());

        if (StringUtils.hasText(status)) {
            updateChain.set(FEEDBACK.STATUS, status);
            // 已解决状态自动记录解决时间
            if ("resolved".equals(status)) {
                updateChain.set(FEEDBACK.RESOLVED_AT, LocalDateTime.now());
            }
        }
        if (StringUtils.hasText(priority)) {
            updateChain.set(FEEDBACK.PRIORITY, priority);
        }

        updateChain.where(FEEDBACK.ID.eq(id)).update();
    }

    @Override
    @Transactional
    public void assignFeedback(UUID id, UUID assigneeId) {
        requireFeedback(id);

        UpdateChain.of(Feedback.class)
                .set(FEEDBACK.ASSIGNEE_ID, assigneeId)
                .set(FEEDBACK.UPDATED_AT, LocalDateTime.now())
                .where(FEEDBACK.ID.eq(id))
                .update();
    }

    /**
     * 统一检查反馈是否存在
     */
    private Feedback requireFeedback(UUID id) {
        if (id == null) {
            throw FeedbackBusinessException.invalidParameter("反馈ID不能为空");
        }
        Feedback feedback = feedbackMapper.selectOneById(id);
        if (feedback == null) {
            throw FeedbackBusinessException.notFound();
        }
        return feedback;
    }
}
