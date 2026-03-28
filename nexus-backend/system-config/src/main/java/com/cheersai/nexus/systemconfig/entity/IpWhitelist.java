package com.cheersai.nexus.systemconfig.entity;

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
 * IP白名单实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("nexus.ip_whitelist")
public class IpWhitelist {

    /**
     * 记录ID
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * IP地址
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * 备注
     */
    @Column("remark")
    private String remark;

    /**
     * 创建人
     */
    @Column("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}