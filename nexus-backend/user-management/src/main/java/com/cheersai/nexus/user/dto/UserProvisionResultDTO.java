package com.cheersai.nexus.user.dto;

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
public class UserProvisionResultDTO {
    private String userId;
    private String email;
    private String username;
    private String status;
    private String ssoOwner;
    private String ssoSubjectId;
    private String ssoUsername;
    private String filebayBaseUrl;
    private String filebayUsername;
    private String filebayRepo;
    private String filebayBranch;
    private String filebayMaskedDir;
    private List<String> createdResources;
    private LocalDateTime provisionedAt;
}
