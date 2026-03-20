package com.cheersai.nexus.feedback.dto;

import lombok.Data;

import java.util.UUID;

/**
 * 反馈分配 DTO
 */
@Data
public class FeedbackAssignDTO {
    private UUID assigneeId;
}
