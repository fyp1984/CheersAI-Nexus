package com.cheersai.nexus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListQueryDTO {
    private String keyword;
    private String status;
    private String role;
    private String memberPlanCode;
    private Integer page;
    private Integer pageSize;
}

