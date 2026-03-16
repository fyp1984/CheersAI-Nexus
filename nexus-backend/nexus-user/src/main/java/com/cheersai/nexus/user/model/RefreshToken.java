package com.cheersai.nexus.user.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

import static com.mybatisflex.annotation.KeyType.Auto;

/**
 * 刷新令牌实体
 */
@Data
@Table("sys_refresh_token")
public class RefreshToken {

    /**
     * ID
     */
    @Id(keyType = Auto)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 刷新令牌
     */
    private String token;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否撤销
     */
    private Boolean revoked;
}
