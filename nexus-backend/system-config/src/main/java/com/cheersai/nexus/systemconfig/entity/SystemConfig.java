package com.cheersai.nexus.systemconfig.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.system_configs")
public class SystemConfig {

    /**
     * 配置ID
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 配置类别：register/login/security/token
     */
    @Column("config_category")
    private String configCategory;

    /**
     * 配置键
     */
    @Column("config_key")
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    @Column("config_value")
    private String configValue;

    /**
     * 配置描述
     */
    @Column("config_desc")
    private String configDesc;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}