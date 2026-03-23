package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品创建DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

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
    private String iconUrl;

    /**
     * 产品状态：active-启用, inactive-停用
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
}
