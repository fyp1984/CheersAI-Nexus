package com.cheersai.nexus.feedback.service.impl;

import com.cheersai.nexus.feedback.dto.request.FeedbackSubmitRequest;
import com.cheersai.nexus.feedback.entity.Feedback;
import com.cheersai.nexus.feedback.mapper.FeedbackMapper;
import com.cheersai.nexus.feedback.service.FeedbackExtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.cheersai.nexus.feedback.entity.table.FeedbackTableDef.FEEDBACK;

@Service
@RequiredArgsConstructor
public class FeedbackExtServiceImpl implements FeedbackExtService {

    private final FeedbackMapper feedbackMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public UUID submitFeedback(FeedbackSubmitRequest request) {
        // Build external user info JSON
        Map<String, Object> externalUserInfo = new HashMap<>();
        externalUserInfo.put("name", request.getName());
        externalUserInfo.put("email", request.getEmail());
        externalUserInfo.put("ssoUserId", request.getSsoUserId());
        externalUserInfo.put("clientVersion", request.getClientVersion());
        externalUserInfo.put("deviceInfo", request.getDeviceInfo());
        externalUserInfo.put("diagnosticLogs", request.getDiagnosticLogs());

        String externalUserInfoJson;
        try {
            externalUserInfoJson = objectMapper.writeValueAsString(externalUserInfo);
        } catch (Exception e) {
            externalUserInfoJson = "{}";
        }

        Feedback feedback = Feedback.builder()
                .source("external")
                .externalUserInfo(externalUserInfoJson)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .attachments(request.getAttachments())
                .status("pending")
                .priority("medium")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    @Override
    public long countExternalSince(LocalDateTime since) {
        return feedbackMapper.selectCountByQuery(
                QueryWrapper.create().select().from(FEEDBACK)
                        .where(FEEDBACK.SOURCE.eq("external"))
                        .and(FEEDBACK.CREATED_AT.ge(since))
        );
    }
}
