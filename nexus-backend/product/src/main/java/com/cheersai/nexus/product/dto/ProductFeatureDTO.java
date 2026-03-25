package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产品功能配置项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeatureDTO {
    private String key;
    private String name;
    private String desc;
    private Boolean enabled;
    private List<String> planCodes;
}
