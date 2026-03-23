package com.cheersai.nexus.product.entity;

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
 * 产品版本实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product_versions")
public class ProductVersion {

    /**
     * 版本ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 关联的产品ID
     */
    @Column("product_id")
    private String productId;

    /**
     * 版本号（如 1.0.0, 1.0.1）
     */
    private String version;

    /**
     * 版本名称（如 正式版Beta版）
     */
    @Column("version_name")
    private String versionName;

    /**
     * 版本状态：draft-草稿, published-已发布, deprecated-已废弃
     */
    private String status;

    /**
     * 更新日志 - Markdown格式
     */
    @Column("changelog")
    private String changelog;

    /**
     * 下载地址 - JSONB格式
     */
    @Column("download_urls")
    private String downloadUrls;

    /**
     * 发布说明
     */
    @Column("release_note")
    private String releaseNote;

    /**
     * 是否强制更新
     */
    @Column("force_update")
    private Boolean forceUpdate;

    /**
     * 最低版本要求（用于增量更新）
     */
    @Column("min_version")
    private String minVersion;

    /**
     * 发布时间
     */
    @Column("published_at")
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @Column("created_by")
    private String createdBy;

    /**
     * 创建人名称
     */
    @Column("created_by_name")
    private String createdByName;
}
