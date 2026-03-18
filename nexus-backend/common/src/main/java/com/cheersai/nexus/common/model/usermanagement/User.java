package com.cheersai.nexus.common.model.usermanagement;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private String userId;

    private String email;

    private String phone;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String passwordHash;

    private String status;

    @Builder.Default
    private Boolean emailVerified = false;

    @Builder.Default
    private Boolean phoneVerified = false;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 逻辑删除相关
    @Column(ignore = true)
    private Boolean deleted;

    // 乐观锁版本号
    @Column(ignore = true)
    private Integer version;
}
