package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品操作日志查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOperationLogQueryDTO {

    private String keyword;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer pageSize;
}
