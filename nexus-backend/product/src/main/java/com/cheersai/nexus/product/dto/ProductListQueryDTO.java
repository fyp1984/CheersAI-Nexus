package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品列表查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListQueryDTO {

    private String keyword;
    private String status;
    private String currentVersion;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer pageSize;
}
