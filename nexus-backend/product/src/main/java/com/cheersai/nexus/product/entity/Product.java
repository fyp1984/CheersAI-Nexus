package com.cheersai.nexus.product.entity;

import com.cheersai.nexus.common.config.PostgreSqlJsonbTypeHandler;
import com.cheersai.nexus.common.config.PostgreSqlUuidStringTypeHandler;
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
 * 产品实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.products")
public class Product {

    /**
     * 产品ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidStringTypeHandler.class)
    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品编码 - 唯一标识
     */
    private String code;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品图标URL
     */
    @Column("icon_url")
    private String iconUrl;

    /**
     * 产品状态：active-启用, inactive-停用, deprecated-已废弃
     */
    private String status;

    /**
     * 当前版本号
     */
    @Column("current_version")
    private String currentVersion;

    /**
     * 下载地址 - JSONB格式存储多个下载地址
     * 结构: [{"platform": "windows", "url": "https://...", "version": "1.0.0"}, ...]
     */
    @Column(value = "download_urls", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String downloadUrls;

    /**
     * 产品设置 - JSONB格式存储功能开关等配置
     * 结构: {"featureFlags": {"darkMode": true, "apiAccess": false}, "customConfig": {...}}
     */
    @Column(value = "settings", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String settings;

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
