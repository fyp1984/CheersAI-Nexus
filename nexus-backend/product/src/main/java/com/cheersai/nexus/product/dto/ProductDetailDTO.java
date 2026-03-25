package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {

    /**
     * 产品ID
     */
    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品编码
     */
    private String code;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品图标URL
     */
    private String iconUrl;

    /**
     * 产品状态
     */
    private String status;

    /**
     * 当前版本号
     */
    private String currentVersion;

    /**
     * 下载地址 - JSON格式
     */
    private String downloadUrls;

    /**
     * 产品设置 - JSON格式
     */
    private String settings;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
