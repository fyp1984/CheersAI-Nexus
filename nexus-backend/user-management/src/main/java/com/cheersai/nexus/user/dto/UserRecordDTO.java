package com.cheersai.nexus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRecordDTO {
    private String userId;
    private String email;
    private String phone;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;
    private String role;
    private String memberPlanCode;
    private LocalDateTime memberExpireAt;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

