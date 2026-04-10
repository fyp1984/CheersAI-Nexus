package com.cheersai.nexus.membership.entity;

import com.cheersai.nexus.common.config.PostgreSqlJsonbTypeHandler;
import com.cheersai.nexus.common.config.PostgreSqlUuidTypeHandler;
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
@Table("desktop_member_events")
public class DesktopMemberEvent {

    @Id(keyType = KeyType.Generator, value = "uuid")
    @Column(typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID id;

    @Column(value = "member_id", typeHandler = PostgreSqlUuidTypeHandler.class)
    private UUID memberId;

    @Column("event_type")
    private String eventType;

    @Column(value = "event_data", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String eventData;

    @Column("session_id")
    private String sessionId;

    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column(value = "device_info", typeHandler = PostgreSqlJsonbTypeHandler.class)
    private String deviceInfo;

    @Column("occurred_at")
    private LocalDateTime occurredAt;

    @Column("created_at")
    private LocalDateTime createdAt;
}
