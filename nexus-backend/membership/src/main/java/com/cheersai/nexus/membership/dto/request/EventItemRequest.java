package com.cheersai.nexus.membership.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventItemRequest {

    private String ssoUserId;
    private String email;
    private String name;
    private String eventType;
    private String eventData;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private LocalDateTime occurredAt;
}
