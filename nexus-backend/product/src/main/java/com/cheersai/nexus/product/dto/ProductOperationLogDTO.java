package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品操作日志 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOperationLogDTO {

    private String id;
    private String productId;
    private String productCode;
    private String productName;
    private String action;
    private String targetType;
    private String targetId;
    private String content;
    private String beforeData;
    private String afterData;
    private String operatorId;
    private String operatorName;
    private String ipAddress;
    private LocalDateTime createdAt;
}
