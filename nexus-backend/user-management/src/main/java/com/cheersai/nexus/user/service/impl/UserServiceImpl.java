package com.cheersai.nexus.user.service.impl;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.user.config.BetaNotificationProperties;
import com.cheersai.nexus.user.dto.BetaApplyRequestDTO;
import com.cheersai.nexus.user.dto.ResetPasswordResponseDTO;
import com.cheersai.nexus.user.dto.UserProvisionResultDTO;
import com.cheersai.nexus.user.dto.UserCreateDTO;
import com.cheersai.nexus.user.dto.UserListQueryDTO;
import com.cheersai.nexus.user.dto.UserListResponseDTO;
import com.cheersai.nexus.user.dto.UserRecordDTO;
import com.cheersai.nexus.user.dto.UserStatusBatchUpdateDTO;
import com.cheersai.nexus.user.dto.UserUpdateDTO;
import com.cheersai.nexus.user.mapper.UserMapper;
import com.cheersai.nexus.user.service.BetaNotificationService;
import com.cheersai.nexus.user.service.BetaProvisioningService;
import com.cheersai.nexus.user.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> VALID_STATUS = Set.of("active", "inactive", "disabled", "deleted");
    private static final Set<String> VALID_ROLE = Set.of("user", "support", "operator", "admin");
    private static final Set<String> VALID_MEMBER_PLAN = Set.of("free", "pro", "pro_team", "enterprise");
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String BETA_DEFAULT_MEMBER_PLAN = "free";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_INACTIVE = "inactive";
    private static final String STATUS_DISABLED = "disabled";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BetaProvisioningService betaProvisioningService;
    private final BetaNotificationProperties betaNotificationProperties;
    private final BetaNotificationService betaNotificationService;

    @Override
    public UserListResponseDTO getUsers(UserListQueryDTO queryDTO) {
        UserListQueryDTO safeQuery = queryDTO != null ? queryDTO : new UserListQueryDTO();
        int page = safePage(safeQuery.getPage());
        int pageSize = safePageSize(safeQuery.getPageSize());
        String status = normalize(safeQuery.getStatus());
        String role = normalize(safeQuery.getRole());
        String memberPlanCode = normalize(safeQuery.getMemberPlanCode());

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(User.class)
                .where(User::getStatus).eq(status, StringUtils.hasText(status))
                .orderBy("created_at", false);

        List<User> allUsers = userMapper.selectListByQuery(queryWrapper);
        List<User> keywordFiltered = filterByKeyword(allUsers, normalize(safeQuery.getKeyword()));

        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= keywordFiltered.size()) {
            return UserListResponseDTO.builder()
                    .items(Collections.emptyList())
                    .total((long) keywordFiltered.size())
                    .build();
        }

        int toIndex = Math.min(fromIndex + pageSize, keywordFiltered.size());
        List<UserRecordDTO> items = keywordFiltered.subList(fromIndex, toIndex).stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());

        return UserListResponseDTO.builder()
                .items(items)
                .total((long) keywordFiltered.size())
                .build();
    }

    @Override
    public List<UserRecordDTO> searchUsers(String userCondition) {
        String keyword = normalize(userCondition);
        if (!StringUtils.hasText(keyword)) {
            throw new RuntimeException("搜索关键字不能为空");
        }
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .orderBy("created_at", false)
        );
        return filterByKeyword(users, keyword).stream().map(this::toRecordDTO).collect(Collectors.toList());
    }

    @Override
    public UserRecordDTO getUserInfoById(String userId) {
        return toRecordDTO(requireUser(userId));
    }

    @Override
    @Transactional
    public UserRecordDTO createUser(UserCreateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求体不能为空");
        }

        String username = normalize(dto.getUsername());
        String nickname = normalize(dto.getNickname());
        String email = normalize(dto.getEmail());
        String phone = normalize(dto.getPhone());
        String password = normalize(dto.getPassword());
        String status = normalize(dto.getStatus());
        String role = normalize(dto.getRole());
        String memberPlanCode = normalize(dto.getMemberPlanCode());

        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (!StringUtils.hasText(nickname)) {
            throw new RuntimeException("昵称不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }
        ensureContactExists(email, phone);

        status = StringUtils.hasText(status) ? status : "active";
        role = StringUtils.hasText(role) ? role : "user";
        memberPlanCode = StringUtils.hasText(memberPlanCode) ? memberPlanCode : "free";
        ensureValidStatus(status);
        ensureValidRole(role);
        ensureValidMemberPlan(memberPlanCode);

        ensureUsernameUnique(username, null);
        ensureEmailUnique(email, null);
        ensurePhoneUnique(phone, null);

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .nickname(nickname)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .status(status)
                .emailVerified(false)
                .phoneVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userMapper.insert(user);
        return toRecordDTO(user);
    }

    @Override
    @Transactional
    public UserRecordDTO updateUser(String userId, UserUpdateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求体不能为空");
        }
        User user = requireUser(userId);
        String previousStatus = normalize(user.getStatus());

        String username = normalize(dto.getUsername());
        String nickname = normalize(dto.getNickname());
        String email = normalize(dto.getEmail());
        String phone = normalize(dto.getPhone());
        String status = normalize(dto.getStatus());
        String role = normalize(dto.getRole());
        String memberPlanCode = normalize(dto.getMemberPlanCode());

        if (StringUtils.hasText(username) && !username.equalsIgnoreCase(user.getUsername())) {
            ensureUsernameUnique(username, user.getId());
            user.setUsername(username);
        }
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        if (dto.getEmail() != null) {
            ensureEmailUnique(email, user.getId());
            user.setEmail(email);
        }
        if (dto.getPhone() != null) {
            ensurePhoneUnique(phone, user.getId());
            user.setPhone(phone);
        }
        ensureContactExists(user.getEmail(), user.getPhone());

        if (StringUtils.hasText(status)) {
            ensureValidStatus(status);
            user.setStatus(status);
        }
        if (StringUtils.hasText(role)) {
            ensureValidRole(role);
            user.setRole(role);
        }
        if (StringUtils.hasText(memberPlanCode)) {
            ensureValidMemberPlan(memberPlanCode);
            user.setMemberPlanCode(memberPlanCode);
        }
        if (dto.getMemberExpireAt() != null || dto.getMemberPlanCode() != null) {
            user.setMemberExpireAt(dto.getMemberExpireAt());
        }

        maybeProvisionWhenActivating(user, previousStatus, normalize(user.getStatus()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        maybeNotifyWhenRejected(user, previousStatus, normalize(user.getStatus()));
        return toRecordDTO(user);
    }

    @Override
    @Transactional
    public UserRecordDTO updateUserStatus(String userId, String status) {
        User user = requireUser(userId);
        String previousStatus = normalize(user.getStatus());
        String safeStatus = normalize(status);
        ensureValidStatus(safeStatus);

        maybeProvisionWhenActivating(user, previousStatus, safeStatus);
        user.setStatus(safeStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        maybeNotifyWhenRejected(user, previousStatus, safeStatus);
        return toRecordDTO(user);
    }

    @Override
    @Transactional
    public void updateBatchStatus(UserStatusBatchUpdateDTO dto) {
        if (dto == null || dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new RuntimeException("用户ID列表不能为空");
        }
        String status = normalize(dto.getStatus());
        ensureValidStatus(status);

        for (String userId : dto.getIds()) {
            User user = requireUser(userId);
            String previousStatus = normalize(user.getStatus());
            maybeProvisionWhenActivating(user, previousStatus, status);
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            maybeNotifyWhenRejected(user, previousStatus, status);
        }
    }

    @Override
    @Transactional
    public ResetPasswordResponseDTO resetPassword(String userId) {
        User user = requireUser(userId);
        String ssoUsername = betaProvisioningService.resetSsoPassword(user, DEFAULT_PASSWORD);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return ResetPasswordResponseDTO.builder()
                .resetTo(DEFAULT_PASSWORD)
                .ssoUsername(ssoUsername)
                .build();
    }

    @Override
    public UserProvisionResultDTO getProvisionResult(String userId) {
        return betaProvisioningService.buildProvisionResult(requireUser(userId));
    }

    @Override
    @Transactional
    public UserRecordDTO applyBeta(BetaApplyRequestDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求体不能为空");
        }

        String name = normalize(dto.getName());
        String email = normalize(dto.getEmail());
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("姓名不能为空");
        }
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        User existingUser = findByEmailIgnoreCase(normalizedEmail);
        if (existingUser != null) {
            if ("active".equalsIgnoreCase(existingUser.getStatus())) {
                throw new RuntimeException("该邮箱已开通，无需重复申请");
            }
            existingUser.setStatus("inactive");
            existingUser.setNickname(name);
            existingUser.setUpdatedAt(LocalDateTime.now());
            userMapper.update(existingUser);
            betaNotificationService.notifySubmitted(existingUser, dto.getLanguage());
            return toRecordDTO(existingUser);
        }

        String username = ensureUniqueUsername(generateUsernameFromEmail(normalizedEmail));
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .nickname(name)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(generateRandomPassword()))
                .status("inactive")
                .role("user")
                .memberPlanCode(BETA_DEFAULT_MEMBER_PLAN)
                .emailVerified(false)
                .phoneVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userMapper.insert(user);
        betaNotificationService.notifySubmitted(user, dto.getLanguage());
        return toRecordDTO(user);
    }

    private User requireUser(String userId) {
        String safeUserId = normalize(userId);
        if (!StringUtils.hasText(safeUserId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = userMapper.selectOneById(safeUserId);
        if (user == null) {
            throw new RuntimeException("USER NOT FOUND");
        }
        return user;
    }

    private void ensureContactExists(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new RuntimeException("邮箱和手机号至少填写一个");
        }
    }

    private void ensureUsernameUnique(String username, String excludeUserId) {
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .where(User::getUsername).isNotNull()
        );
        boolean exists = users.stream().anyMatch(item -> StringUtils.hasText(item.getUsername())
                && item.getUsername().equalsIgnoreCase(username)
                && (excludeUserId == null || !excludeUserId.equals(item.getId())));
        if (exists) {
            throw new RuntimeException("USERNAME EXISTS");
        }
    }

    private void ensureEmailUnique(String email, String excludeUserId) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .where(User::getEmail).isNotNull()
        );
        boolean exists = users.stream().anyMatch(item -> StringUtils.hasText(item.getEmail())
                && item.getEmail().equalsIgnoreCase(email)
                && (excludeUserId == null || !excludeUserId.equals(item.getId())));
        if (exists) {
            throw new RuntimeException("EMAIL ALREADY EXISTS");
        }
    }

    private void ensurePhoneUnique(String phone, String excludeUserId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .where(User::getPhone).isNotNull()
        );
        boolean exists = users.stream().anyMatch(item -> StringUtils.hasText(item.getPhone())
                && item.getPhone().equals(phone)
                && (excludeUserId == null || !excludeUserId.equals(item.getId())));
        if (exists) {
            throw new RuntimeException("PHONE_EXISTS");
        }
    }

    private void ensureValidStatus(String status) {
        if (!StringUtils.hasText(status) || !VALID_STATUS.contains(status)) {
            throw new RuntimeException("INVALID_STATUS");
        }
    }

    private void ensureValidRole(String role) {
        if (!StringUtils.hasText(role) || !VALID_ROLE.contains(role)) {
            throw new RuntimeException("INVALID_ROLE");
        }
    }

    private void ensureValidMemberPlan(String memberPlanCode) {
        if (!StringUtils.hasText(memberPlanCode) || !VALID_MEMBER_PLAN.contains(memberPlanCode)) {
            throw new RuntimeException("会员方案编码不合法");
        }
    }

    private List<User> filterByKeyword(List<User> users, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return users;
        }
        String needle = keyword.toLowerCase(Locale.ROOT);
        return users.stream()
                .filter(user -> containsIgnoreCase(user.getId(), needle)
                        || containsIgnoreCase(user.getUsername(), needle)
                        || containsIgnoreCase(user.getNickname(), needle)
                        || containsIgnoreCase(user.getEmail(), needle)
                        || containsIgnoreCase(user.getPhone(), needle)
                        || containsIgnoreCase(user.getStatus(), needle)
                        || containsIgnoreCase(user.getRole(), needle)
                        || containsIgnoreCase(user.getMemberPlanCode(), needle))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private int safePage(Integer page) {
        return page != null && page > 0 ? page : 1;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void maybeProvisionWhenActivating(User user, String previousStatus, String targetStatus) {
        if (!STATUS_ACTIVE.equalsIgnoreCase(normalize(targetStatus))) {
            return;
        }
        if (STATUS_ACTIVE.equalsIgnoreCase(normalize(previousStatus))) {
            return;
        }
        if (!betaProvisioningService.isEnabled()) {
            throw new RuntimeException("内测开通流程未启用，禁止直接审核通过。请先开启 BETA_PROVISION_ENABLED。");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new RuntimeException("用户缺少邮箱，无法执行开通流程");
        }
        try {
            BetaProvisioningService.ProvisioningResult provisioningResult = betaProvisioningService.provisionForActivation(user);
            betaNotificationService.notifyApproved(
                    user,
                    provisioningResult.ssoUsername(),
                    provisioningResult.filebayRepo(),
                    provisioningResult.ssoInitialPassword()
            );
        } catch (RuntimeException ex) {
            betaNotificationService.notifyProvisionFailed(user, ex.getMessage());
            throw ex;
        }
    }

    private void maybeNotifyWhenRejected(User user, String previousStatus, String targetStatus) {
        if (!STATUS_INACTIVE.equalsIgnoreCase(normalize(previousStatus))) {
            return;
        }
        if (!STATUS_DISABLED.equalsIgnoreCase(normalize(targetStatus))) {
            return;
        }
        betaNotificationService.notifyRejected(user, betaNotificationProperties.getRejectedReason());
    }

    private User findByEmailIgnoreCase(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .where(User::getEmail).isNotNull()
        );
        return users.stream()
                .filter(item -> StringUtils.hasText(item.getEmail()) && item.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    private String generateUsernameFromEmail(String email) {
        String base = email.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(base)) {
            base = "beta_user";
        }
        if (base.length() > 24) {
            base = base.substring(0, 24);
        }
        String suffix = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(UUID.nameUUIDFromBytes(email.getBytes()).toString().getBytes())
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
        suffix = suffix.length() > 6 ? suffix.substring(0, 6) : suffix;
        return base + "_" + suffix;
    }

    private String ensureUniqueUsername(String candidate) {
        String username = candidate;
        int attempt = 1;
        while (usernameExists(username)) {
            username = candidate + "_" + attempt;
            attempt++;
            if (attempt > 9999) {
                throw new RuntimeException("用户名生成失败，请稍后重试");
            }
        }
        return username;
    }

    private boolean usernameExists(String username) {
        List<User> users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(User.class)
                        .where(User::getUsername).isNotNull()
        );
        return users.stream().anyMatch(item -> username.equalsIgnoreCase(item.getUsername()));
    }

    private String generateRandomPassword() {
        return "Aa1!" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private UserRecordDTO toRecordDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserRecordDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .role(user.getRole())
                .memberPlanCode(user.getMemberPlanCode())
                .memberExpireAt(user.getMemberExpireAt())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

