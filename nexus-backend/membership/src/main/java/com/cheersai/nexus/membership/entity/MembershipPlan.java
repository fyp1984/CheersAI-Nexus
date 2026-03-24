package com.cheersai.nexus.membership.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员计划实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("membership_plans")
public class MembershipPlan {

    /**
     * 计划ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 计划编码 - 唯一标识（如 free, pro, team, enterprise）
     */
    private String code;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 月付价格
     */
    @Column("price_monthly")
    private BigDecimal priceMonthly;

    /**
     * 年付价格
     */
    @Column("price_yearly")
    private BigDecimal priceYearly;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 权益配置 - JSONB格式
     */
    private String features;

    /**
     * 额度配置 - JSONB格式
     */
    private String limits;

    /**
     * 排序顺序
     */
    @Column("sort_order")
    private Integer sortOrder;

    /**
     * 状态：active-启用, disabled-禁用
     */
    private String status;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
