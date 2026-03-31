package com.cheersai.nexus.product.entity;

import com.cheersai.nexus.common.config.PostgreSqlJsonbTypeHandler;
import com.cheersai.nexus.common.config.PostgreSqlUuidStringTypeHandler;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品操作日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.product_operation_logs")
public class ProductOperationLog {

    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidStringTypeHandler.class)
    private String id;

    @Column(value = "product_id", typeHandler = PostgreSqlUuidStringTypeHandler.class)
    private String productId;

    @Column("product_code")
    private String productCode;

    @Column("product_name")
    private String productName;

    private String action;

    @Column("target_type")
    private String targetType;

    @Column("target_id")
    private String targetId;

    private String content;

    @Column(value = "before_data", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String beforeData;

    @Column(value = "after_data", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String afterData;

    @Column("operator_id")
    private String operatorId;

    @Column("operator_name")
    private String operatorName;

    @Column("ip_address")
    private String ipAddress;

    @Column("created_at")
    private LocalDateTime createdAt;
}
