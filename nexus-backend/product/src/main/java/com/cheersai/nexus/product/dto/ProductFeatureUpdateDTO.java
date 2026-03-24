package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产品功能配置更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeatureUpdateDTO {
    private List<ProductFeatureDTO> features;
}
