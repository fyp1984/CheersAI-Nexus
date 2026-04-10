package com.cheersai.nexus.membership.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesktopMemberDetailResponse {

    private String id;
    private String ssoUserId;
    private String email;
    private String name;
    private String avatarUrl;
    private String status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime lastActiveAt;
    private String appVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> recentEventTypes;
    private long feedbackCount;
}
