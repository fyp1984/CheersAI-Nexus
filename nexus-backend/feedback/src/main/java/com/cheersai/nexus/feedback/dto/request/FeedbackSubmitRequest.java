package com.cheersai.nexus.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackSubmitRequest {

    @NotBlank(message = "ssoUserId不能为空")
    private String ssoUserId;

    private String email;
    private String name;

    @NotBlank(message = "反馈类型不能为空")
    private String type;

    @NotBlank(message = "反馈标题不能为空")
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    private String attachments;
    private String clientVersion;
    private String deviceInfo;
    private String diagnosticLogs;
}
