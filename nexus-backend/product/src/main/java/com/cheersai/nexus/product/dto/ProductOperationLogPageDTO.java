package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产品操作日志分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOperationLogPageDTO {

    private List<ProductOperationLogDTO> items;
    private Long total;
}
