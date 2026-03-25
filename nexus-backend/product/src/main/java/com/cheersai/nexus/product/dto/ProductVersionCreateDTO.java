package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本创建DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionCreateDTO {

    /**
     * 版本号（如 1.0.0）
     */
    private String version;

    /**
     * 版本名称（如 正式版、Beta版）
     */
    private String versionName;

    /**
     * 更新日志 - Markdown格式
     */
    private String changelog;

    /**
     * 下载地址 - JSON格式
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
}
