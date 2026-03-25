package com.cheersai.nexus.feedback.entity;

import com.cheersai.nexus.feedback.config.PostgreSqlUuidTypeHandler;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feedbacks")
public class Feedback {

    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID id;

    // 合并列名和类型处理器
    @Column(value = "user_id", typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID userId;

    // 合并列名和类型处理器
    @Column(value = "product_id", typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID productId;

    private String type;

    private String title;

    private String content;

    private String attachments;

    private String status;

    private String priority;

    // 合并列名和类型处理器
    @Column(value = "assignee_id", typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID assigneeId;

    @Column("resolved_at")
    private LocalDateTime resolvedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}