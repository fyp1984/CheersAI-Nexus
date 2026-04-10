package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.dto.response.DesktopMemberDetailResponse;
import com.cheersai.nexus.membership.dto.response.DesktopMemberStatsResponse;
import com.cheersai.nexus.membership.entity.DesktopMember;
import com.cheersai.nexus.membership.entity.DesktopMemberEvent;
import com.cheersai.nexus.membership.service.DesktopMemberEventService;
import com.cheersai.nexus.membership.service.DesktopMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/desktop-members")
@RequiredArgsConstructor
public class DesktopMemberController {

    private final DesktopMemberService memberService;
    private final DesktopMemberEventService eventService;

    @GetMapping
    public Result<Map<String, Object>> listMembers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {

        List<DesktopMember> members = memberService.listMembers(page, pageSize, keyword, status);
        long total = memberService.countMembers(keyword, status);

        List<Map<String, Object>> items = members.stream()
                .map(this::toMemberMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<DesktopMemberStatsResponse> getStats() {
        return Result.success(eventService.getStats());
    }

    @GetMapping("/{id}")
    public Result<DesktopMemberDetailResponse> getMemberDetail(@PathVariable UUID id) {
        DesktopMember member = memberService.getById(id)
                .orElseThrow(() -> new RuntimeException("会员不存在"));

        List<DesktopMemberEvent> recentEvents = eventService.getMemberEvents(id, 1, 10);
        List<String> recentEventTypes = recentEvents.stream()
                .map(DesktopMemberEvent::getEventType)
                .distinct()
                .collect(Collectors.toList());

        DesktopMemberDetailResponse response = DesktopMemberDetailResponse.builder()
                .id(member.getId() != null ? member.getId().toString() : null)
                .ssoUserId(member.getSsoUserId())
                .email(member.getEmail())
                .name(member.getName())
                .avatarUrl(member.getAvatarUrl())
                .status(member.getStatus())
                .lastLoginAt(member.getLastLoginAt())
                .lastLoginIp(member.getLastLoginIp())
                .lastActiveAt(member.getLastActiveAt())
                .appVersion(member.getAppVersion())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .recentEventTypes(recentEventTypes)
                .feedbackCount(0)
                .build();

        return Result.success(response);
    }

    @GetMapping("/{id}/events")
    public Result<Map<String, Object>> getMemberEvents(
            @PathVariable UUID id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

        List<DesktopMemberEvent> events = eventService.getMemberEvents(id, page, pageSize);
        long total = eventService.countMemberEvents(id);

        Map<String, Object> result = new HashMap<>();
        result.put("items", events);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/{id}/login-history")
    public Result<Map<String, Object>> getLoginHistory(
            @PathVariable UUID id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

        List<DesktopMemberEvent> history = eventService.getLoginHistory(id, page, pageSize);
        long total = eventService.countLoginHistory(id);

        Map<String, Object> result = new HashMap<>();
        result.put("items", history);
        result.put("total", total);
        return Result.success(result);
    }

    private Map<String, Object> toMemberMap(DesktopMember m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId() != null ? m.getId().toString() : null);
        map.put("ssoUserId", m.getSsoUserId());
        map.put("email", m.getEmail());
        map.put("name", m.getName());
        map.put("avatarUrl", m.getAvatarUrl());
        map.put("status", m.getStatus());
        map.put("lastLoginAt", m.getLastLoginAt());
        map.put("lastLoginIp", m.getLastLoginIp());
        map.put("lastActiveAt", m.getLastActiveAt());
        map.put("appVersion", m.getAppVersion());
        map.put("createdAt", m.getCreatedAt());
        map.put("updatedAt", m.getUpdatedAt());
        return map;
    }
}
