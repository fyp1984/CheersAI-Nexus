package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产品批量删除请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchDeleteDTO {
    private List<String> ids;
}
