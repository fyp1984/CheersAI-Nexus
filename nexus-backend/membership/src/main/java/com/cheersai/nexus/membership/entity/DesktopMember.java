package com.cheersai.nexus.membership.entity;

import com.cheersai.nexus.common.config.PostgreSqlJsonbTypeHandler;
import com.cheersai.nexus.common.config.PostgreSqlUuidTypeHandler;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("desktop_members")
public class DesktopMember {

    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID id;

    @Column("sso_user_id")
    private String ssoUserId;

    private String email;

    private String name;

    @Column("avatar_url")
    private String avatarUrl;

    private String status;

    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    @Column("last_login_ip")
    private String lastLoginIp;

    @Column("last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(value = "device_info", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String deviceInfo;

    @Column("app_version")
    private String appVersion;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
