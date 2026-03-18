package com.cheersai.nexus.user.model.dto;

import com.cheersai.nexus.user.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户状态更新DTO
 */
@Data
public class UserStatusUpdateDTO {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 目标状态
     */
    @NotBlank(message = "状态不能为空")
    private UserStatus status;
}
