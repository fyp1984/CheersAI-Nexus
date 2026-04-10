package com.cheersai.nexus.membership.service.impl;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.client.FeedbackServiceClient;
import com.cheersai.nexus.membership.dto.request.EventItemRequest;
import com.cheersai.nexus.membership.dto.response.DesktopMemberStatsResponse;
import com.cheersai.nexus.membership.entity.DesktopMember;
import com.cheersai.nexus.membership.entity.DesktopMemberEvent;
import com.cheersai.nexus.membership.mapper.DesktopMemberEventMapper;
import com.cheersai.nexus.membership.service.DesktopMemberEventService;
import com.cheersai.nexus.membership.service.DesktopMemberService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.cheersai.nexus.membership.entity.table.DesktopMemberEventTableDef.DESKTOP_MEMBER_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesktopMemberEventServiceImpl implements DesktopMemberEventService {

    private final DesktopMemberEventMapper eventMapper;
    private final DesktopMemberService memberService;
    private final FeedbackServiceClient feedbackServiceClient;

    @Override
    @Transactional
    public void ingestEvents(List<EventItemRequest> events) {
        for (EventItemRequest event : events) {
            if (event.getSsoUserId() == null || event.getEventType() == null) {
                continue;
            }

            DesktopMember member = memberService.upsertFromEvent(
                    event.getSsoUserId(),
                    event.getEmail(),
                    event.getName(),
                    null
            );

            if ("login".equals(event.getEventType())) {
                memberService.updateLastLogin(member.getId(), event.getIpAddress(),
                        event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now());
            } else {
                memberService.updateLastActive(member.getId(), event.getIpAddress());
            }

            DesktopMemberEvent eventRecord = DesktopMemberEvent.builder()
                    .memberId(member.getId())
                    .eventType(event.getEventType())
                    .eventData(event.getEventData())
                    .sessionId(event.getSessionId())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .deviceInfo(event.getDeviceInfo())
                    .occurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            eventMapper.insert(eventRecord);
        }
    }

    @Override
    public List<DesktopMemberEvent> getMemberEvents(UUID memberId, int page, int pageSize) {
        return eventMapper.selectListByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.MEMBER_ID.eq(memberId))
                        .orderBy(DESKTOP_MEMBER_EVENT.OCCURRED_AT.desc())
                        .limit(pageSize)
                        .offset((long) (page - 1) * pageSize)
        );
    }

    @Override
    public long countMemberEvents(UUID memberId) {
        return eventMapper.selectCountByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.MEMBER_ID.eq(memberId))
        );
    }

    @Override
    public List<DesktopMemberEvent> getLoginHistory(UUID memberId, int page, int pageSize) {
        return eventMapper.selectListByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.MEMBER_ID.eq(memberId))
                        .and(DESKTOP_MEMBER_EVENT.EVENT_TYPE.in("login", "logout"))
                        .orderBy(DESKTOP_MEMBER_EVENT.OCCURRED_AT.desc())
                        .limit(pageSize)
                        .offset((long) (page - 1) * pageSize)
        );
    }

    @Override
    public long countLoginHistory(UUID memberId) {
        return eventMapper.selectCountByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.MEMBER_ID.eq(memberId))
                        .and(DESKTOP_MEMBER_EVENT.EVENT_TYPE.in("login", "logout"))
        );
    }

    @Override
    public DesktopMemberStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        long dau = countUniqueMembersWithLoginEventSince(todayStart);
        long wau = countUniqueMembersWithLoginEventSince(weekAgo);
        long mau = countUniqueMembersWithLoginEventSince(monthAgo);

        long todayEvents = countEventsSince(todayStart);

        long todayFeedbacks = fetchTodayExternalFeedbacks(todayStart);

        return DesktopMemberStatsResponse.builder()
                .totalMembers(memberService.countMembers())
                .dau(dau)
                .wau(wau)
                .mau(mau)
                .todayEvents(todayEvents)
                .todayFeedbacks(todayFeedbacks)
                .build();
    }

    /**
     * 通过 Feign 调用 feedback 服务获取今日外部反馈数
     */
    private long fetchTodayExternalFeedbacks(LocalDateTime todayStart) {
        try {
            Result<Long> result = feedbackServiceClient.countExternalSince(todayStart);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("Failed to fetch external feedback count from feedback service: {}", e.getMessage());
            return 0L;
        }
    }

    private long countEventsSince(LocalDateTime since) {
        return eventMapper.selectCountByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.OCCURRED_AT.ge(since))
        );
    }

    private long countUniqueMembersWithLoginEventSince(LocalDateTime since) {
        List<DesktopMemberEvent> loginEvents = eventMapper.selectListByQuery(
                QueryWrapper.create().select("DISTINCT member_id").from(DESKTOP_MEMBER_EVENT)
                        .where(DESKTOP_MEMBER_EVENT.EVENT_TYPE.eq("login"))
                        .and(DESKTOP_MEMBER_EVENT.OCCURRED_AT.ge(since))
        );
        return loginEvents.size();
    }
}
