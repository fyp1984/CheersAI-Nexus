package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产品列表分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDTO {

    private List<ProductDetailDTO> items;
    private Long total;
}
