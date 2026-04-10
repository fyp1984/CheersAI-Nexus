package com.cheersai.nexus.membership.service;

import com.cheersai.nexus.membership.dto.request.EventItemRequest;
import com.cheersai.nexus.membership.dto.response.DesktopMemberStatsResponse;
import com.cheersai.nexus.membership.entity.DesktopMemberEvent;

import java.util.List;
import java.util.UUID;

public interface DesktopMemberEventService {

    void ingestEvents(List<EventItemRequest> events);

    List<DesktopMemberEvent> getMemberEvents(UUID memberId, int page, int pageSize);

    long countMemberEvents(UUID memberId);

    List<DesktopMemberEvent> getLoginHistory(UUID memberId, int page, int pageSize);

    long countLoginHistory(UUID memberId);

    DesktopMemberStatsResponse getStats();
}
