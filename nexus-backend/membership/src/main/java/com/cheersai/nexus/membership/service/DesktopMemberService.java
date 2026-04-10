package com.cheersai.nexus.membership.service;

import com.cheersai.nexus.membership.entity.DesktopMember;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DesktopMemberService {

    DesktopMember upsertFromEvent(String ssoUserId, String email, String name, String avatarUrl);

    Optional<DesktopMember> getBySsoUserId(String ssoUserId);

    Optional<DesktopMember> getById(UUID id);

    void updateLastLogin(UUID memberId, String ipAddress, LocalDateTime loginTime);

    void updateLastActive(UUID memberId, String ipAddress);

    List<DesktopMember> listMembers(int page, int pageSize, String keyword, String status);

    long countMembers();

    long countMembers(String keyword, String status);

    long countActiveSince(LocalDateTime since);
}
