package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品版本详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionDetailDTO {

    /**
     * 版本ID
     */
    private String id;

    /**
     * 关联的产品ID
     */
    private String productId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本状态
     */
    private String status;

    /**
     * 更新日志
     */
    private String changelog;

    /**
     * 下载地址
     */
    private String downloadUrls;

    /**
     * 发布说明
     */
    private String releaseNote;

    /**
     * 是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 最低版本要求
     */
    private String minVersion;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;
}
