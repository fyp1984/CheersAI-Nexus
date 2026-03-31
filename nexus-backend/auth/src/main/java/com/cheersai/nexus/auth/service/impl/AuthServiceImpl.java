package com.cheersai.nexus.auth.service.impl;

import com.cheersai.nexus.auth.config.JwtProperties;
import com.cheersai.nexus.auth.dto.*;
import com.cheersai.nexus.auth.entity.AuditLog;
import com.cheersai.nexus.auth.entity.RefreshToken;
import com.cheersai.nexus.auth.exception.AuthBusinessException;
import com.cheersai.nexus.auth.exception.AuthErrorCode;
import com.cheersai.nexus.auth.mapper.AuditLogMapper;
import com.cheersai.nexus.auth.mapper.RefreshTokenMapper;
import com.cheersai.nexus.auth.mapper.UserMapper;
import com.cheersai.nexus.auth.repository.TokenRepository;
import com.cheersai.nexus.auth.service.AuthService;
import com.cheersai.nexus.auth.util.JwtUtil;
import com.cheersai.nexus.common.model.usermanagement.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UserMapper userMapper;
    
    @Autowired
    private final RefreshTokenMapper refreshTokenMapper;
    
    @Autowired
    private final AuditLogMapper auditLogMapper;
    
    @Autowired
    private final VerificationCodeServiceImpl verificationCodeService;
    
    @Autowired
    private final TokenRepository tokenRepository;
    
    @Autowired
    private final JwtUtil jwtUtil;
    
    @Autowired
    private final JwtProperties jwtProperties;
    
    @Autowired
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        // 验证验证码
        String email = clean(request.getEmail());
        String phone = clean(request.getPhone());
        String username = clean(request.getUsername());
        String target = email != null ? email : phone;

        if (!verificationCodeService.verifyCode(target, request.getCode(), "register")) {
            throw new AuthBusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 检查用户是否已存在
        if (email != null) {
            User existingUser = userMapper.selectOneByEmailIgnoreCase(email);
            if (existingUser != null) {
                throw new AuthBusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        if (phone != null) {
            User existingUser = userMapper.selectOneByPhone(phone);
            if (existingUser != null) {
                throw new AuthBusinessException(AuthErrorCode.PHONE_ALREADY_EXISTS);
            }
        }

        User existingUser = userMapper.selectOneByUsernameIgnoreCase(username);
        if (existingUser != null) {
            throw new AuthBusinessException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 创建用户
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .phone(phone)
                .username(username)
                .nickname(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status("active")
                .emailVerified(email != null)
                .phoneVerified(phone != null)
                .build();

        userMapper.insert(user);

        // 记录审计日志
        saveAuditLog(user.getId(), "info", "register", ipAddress, userAgent, true, null);

        // 生成 Token
        return generateTokens(user, ipAddress, userAgent);
    }

    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = clean(request.getEmail());
        String phone = clean(request.getPhone());
        User user;

        // 根据登录方式查找用户
        if (email != null) {
            user = userMapper.selectOneByEmailIgnoreCase(email);
        } else if (phone != null) {
            user = userMapper.selectOneByPhone(phone);
        } else {
            throw new AuthBusinessException(AuthErrorCode.INVALID_PARAMETER, "请提供邮箱或手机号");
        }

        if (user == null) {
            // 登录失败不记录审计日志（防止日志泛滥）
            throw new AuthBusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 密码错误不记录审计日志（防止日志泛滥）
            throw new AuthBusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            // 账户禁用不记录审计日志（防止日志泛滥）
            throw new AuthBusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userMapper.update(user);

        // 记录审计日志
        saveAuditLog(user.getId(), "info","login", ipAddress, userAgent, true, null);

        // 生成 Token
        return generateTokens(user, ipAddress, userAgent);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        // 验证 refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthBusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new AuthBusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN, "无效的刷新令牌类型");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 从 Redis 获取用户信息
        String cachedUserId = tokenRepository.getUserIdByRefreshToken(refreshToken);
        if (cachedUserId == null || !cachedUserId.equals(userId)) {
            throw new AuthBusinessException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 查询用户
        User user = userMapper.selectOneByUserId(userId);
        if (user == null) {
            throw new AuthBusinessException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 删除旧的 refresh token
        tokenRepository.deleteRefreshToken(refreshToken);

        // 生成新 Token
        return generateTokens(user, ipAddress, userAgent);
    }

    /**
     * 用户登出
     */
    public void logout(String accessToken, String refreshToken, String userId) {
        // 将 token 加入黑名单
        if (accessToken != null) {
            tokenRepository.deleteAccessToken(accessToken);
            tokenRepository.addToBlacklist(accessToken, jwtProperties.getAccessTokenExpiration() / 1000);
        }

        if (refreshToken != null) {
            tokenRepository.deleteRefreshToken(refreshToken);
            tokenRepository.addToBlacklist(refreshToken, jwtProperties.getRefreshTokenExpiration() / 1000);
        }

        log.info("User logged out: {}", userId);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = clean(request.getEmail());
        String phone = clean(request.getPhone());
        String type = email != null ? "email" : "phone";
        String target = email != null ? email : phone;

        // 验证验证码
        if (!verificationCodeService.verifyCode(target, request.getCode(), "reset_password")) {
            throw new AuthBusinessException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 查找用户并更新密码
        User user = type.equals("email")
                ? userMapper.selectOneByEmailIgnoreCase(target)
                : userMapper.selectOneByPhone(target);

        if (user == null) {
            throw new AuthBusinessException(AuthErrorCode.USER_NOT_FOUND);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.update(user);
    }

    /**
     * 生成 Token
     */
    private AuthResponse generateTokens(User user, String ipAddress, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "free" // 默认计划
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        String idToken = jwtUtil.generateIdToken(
                user.getId(),
                user.getEmail(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "free"
        );

        // 存储 Token 到 Redis
        tokenRepository.saveAccessToken(user.getId(), accessToken, jwtProperties.getAccessTokenExpiration() / 1000);
        tokenRepository.saveRefreshToken(user.getId(), refreshToken, jwtProperties.getRefreshTokenExpiration() / 1000);

        // 保存 Refresh Token 到数据库
//        RefreshToken token = RefreshToken.builder()
//                .id(UUID.randomUUID().toString())
//                .userId(user.getId())
//                .token(refreshToken)
//                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
//                .build();
//        refreshTokenMapper.insert(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .idToken(idToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .user(UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    /**
     * 保存审计日志
     */
    private void saveAuditLog(String userId, String logType, String action, String ipAddress,
                              String userAgent, boolean success, String details) {
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID().toString())
                .logType(logType)
                .operatorId(userId)
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result(success ? "success" : "failure")
                .afterData(details)
                .build();
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: userId={}, action={}, error={}", userId, action, e.getMessage());
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
