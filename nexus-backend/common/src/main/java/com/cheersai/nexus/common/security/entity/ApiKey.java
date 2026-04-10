package com.cheersai.nexus.common.security.entity;

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
@Table("api_keys")
public class ApiKey {

    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID id;

    @Column("key_id")
    private String keyId;

    @Column("key_secret_hash")
    private String keySecretHash;

    private String name;

    private String permissions;

    @Column("rate_limit")
    private Integer rateLimit;

    private String status;

    @Column(value = "created_by", typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("last_used_at")
    private LocalDateTime lastUsedAt;
}
