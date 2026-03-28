package com.cheersai.nexus.membership.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户订阅实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.subscriptions")
public class Subscription {

    /**
     * 订阅ID - UUID主键
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 用户ID
     */
    @Column("user_id")
    private String userId;

    /**
     * 会员计划编码
     */
    @Column("plan_code")
    private String planCode;

    /**
     * 订阅状态：active-生效中, expired-已过期, cancelled-已取消
     */
    private String status;

    /**
     * 开始日期
     */
    @Column("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column("end_date")
    private LocalDate endDate;

    /**
     * 是否自动续费
     */
    @Column("auto_renew")
    private Boolean autoRenew;

    /**
     * 支付方式
     */
    @Column("payment_method")
    private String paymentMethod;

    /**
     * 最后支付时间
     */
    @Column("last_payment_at")
    private LocalDateTime lastPaymentAt;

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
