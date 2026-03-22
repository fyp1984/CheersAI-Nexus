package com.cheersai.nexus.feedback.mapper;

import com.cheersai.nexus.feedback.entity.Feedback;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 Mapper 接口
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
