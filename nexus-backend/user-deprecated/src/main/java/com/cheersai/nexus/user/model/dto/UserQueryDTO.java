package com.cheersai.nexus.user.model.dto;

import com.cheersai.nexus.user.enums.UserStatus;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {

    /**
     * 关键词（搜索用户名/邮箱/手机号）
     */
    private String keyword;

    /**
     * 状态
     */
    private UserStatus status;

    /**
     * 邮箱已验证
     */
    private Boolean emailVerified;

    /**
     * 手机已验证
     */
    private Boolean phoneVerified;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
