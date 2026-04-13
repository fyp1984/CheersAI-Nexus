package com.cheersai.nexus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponseDTO {
    private String resetTo;
    private String ssoUsername;
}

