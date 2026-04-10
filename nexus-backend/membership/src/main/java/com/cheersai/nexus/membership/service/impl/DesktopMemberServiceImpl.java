package com.cheersai.nexus.membership.service.impl;

import com.cheersai.nexus.membership.entity.DesktopMember;
import com.cheersai.nexus.membership.mapper.DesktopMemberMapper;
import com.cheersai.nexus.membership.service.DesktopMemberService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cheersai.nexus.membership.entity.table.DesktopMemberTableDef.DESKTOP_MEMBER;

@Service
@RequiredArgsConstructor
public class DesktopMemberServiceImpl implements DesktopMemberService {

    private final DesktopMemberMapper desktopMemberMapper;

    @Override
    @Transactional
    public DesktopMember upsertFromEvent(String ssoUserId, String email, String name, String avatarUrl) {
        DesktopMember existing = desktopMemberMapper.selectOneByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER)
                        .where(DESKTOP_MEMBER.SSO_USER_ID.eq(ssoUserId))
        );

        LocalDateTime now = LocalDateTime.now();

        if (existing != null) {
            if (StringUtils.hasText(email)) existing.setEmail(email);
            if (StringUtils.hasText(name)) existing.setName(name);
            if (StringUtils.hasText(avatarUrl)) existing.setAvatarUrl(avatarUrl);
            existing.setUpdatedAt(now);
            desktopMemberMapper.update(existing);
            return existing;
        }

        DesktopMember member = DesktopMember.builder()
                .ssoUserId(ssoUserId)
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                .status("active")
                .createdAt(now)
                .updatedAt(now)
                .build();
        desktopMemberMapper.insert(member);
        return member;
    }

    @Override
    public Optional<DesktopMember> getBySsoUserId(String ssoUserId) {
        DesktopMember member = desktopMemberMapper.selectOneByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER)
                        .where(DESKTOP_MEMBER.SSO_USER_ID.eq(ssoUserId))
        );
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<DesktopMember> getById(UUID id) {
        return Optional.ofNullable(desktopMemberMapper.selectOneById(id));
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID memberId, String ipAddress, LocalDateTime loginTime) {
        DesktopMember member = desktopMemberMapper.selectOneById(memberId);
        if (member != null) {
            member.setLastLoginAt(loginTime);
            member.setLastLoginIp(ipAddress);
            member.setUpdatedAt(LocalDateTime.now());
            desktopMemberMapper.update(member);
        }
    }

    @Override
    @Transactional
    public void updateLastActive(UUID memberId, String ipAddress) {
        DesktopMember member = desktopMemberMapper.selectOneById(memberId);
        if (member != null) {
            member.setLastActiveAt(LocalDateTime.now());
            if (StringUtils.hasText(ipAddress)) {
                member.setLastLoginIp(ipAddress);
            }
            member.setUpdatedAt(LocalDateTime.now());
            desktopMemberMapper.update(member);
        }
    }

    @Override
    public List<DesktopMember> listMembers(int page, int pageSize, String keyword, String status) {
        QueryWrapper query = QueryWrapper.create()
                .select().from(DESKTOP_MEMBER)
                .where(DESKTOP_MEMBER.STATUS.eq(status, StringUtils.hasText(status)))
                .orderBy(DESKTOP_MEMBER.CREATED_AT.desc());

        List<DesktopMember> all = desktopMemberMapper.selectListByQuery(query);

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.toLowerCase();
            all = all.stream()
                    .filter(m -> containsIgnoreCase(m.getName(), kw)
                            || containsIgnoreCase(m.getEmail(), kw)
                            || containsIgnoreCase(m.getSsoUserId(), kw))
                    .collect(Collectors.toList());
        }

        int from = (page - 1) * pageSize;
        if (from >= all.size()) return List.of();
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    @Override
    public long countMembers() {
        return desktopMemberMapper.selectCountByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER)
        );
    }

    @Override
    public long countMembers(String keyword, String status) {
        QueryWrapper query = QueryWrapper.create()
                .select().from(DESKTOP_MEMBER)
                .where(DESKTOP_MEMBER.STATUS.eq(status, StringUtils.hasText(status)));

        if (!StringUtils.hasText(keyword)) {
            return desktopMemberMapper.selectCountByQuery(query);
        }

        String kw = keyword.toLowerCase();
        List<DesktopMember> all = desktopMemberMapper.selectListByQuery(query);
        return all.stream()
                .filter(m -> containsIgnoreCase(m.getName(), kw)
                        || containsIgnoreCase(m.getEmail(), kw)
                        || containsIgnoreCase(m.getSsoUserId(), kw))
                .count();
    }

    @Override
    public long countActiveSince(LocalDateTime since) {
        return desktopMemberMapper.selectCountByQuery(
                QueryWrapper.create().select().from(DESKTOP_MEMBER)
                        .where(DESKTOP_MEMBER.LAST_ACTIVE_AT.ge(since))
        );
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
