package com.cheersai.nexus.auth.service.impl;

import com.cheersai.nexus.auth.config.JwtProperties;
import com.cheersai.nexus.auth.dto.*;
import com.cheersai.nexus.auth.entity.AuditLog;
import com.cheersai.nexus.auth.entity.RefreshToken;
import com.cheersai.nexus.auth.mapper.AuditLogMapper;
import com.cheersai.nexus.auth.mapper.RefreshTokenMapper;
import com.cheersai.nexus.auth.mapper.UserMapper;
import com.cheersai.nexus.auth.repository.TokenRepository;
import com.cheersai.nexus.auth.service.AuthService;
import com.cheersai.nexus.auth.util.JwtUtil;
import com.cheersai.nexus.common.model.usermanagement.User;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.cheersai.nexus.common.model.usermanagement.table.UserTableDef.USER;

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
    
    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        // 验证验证码
        String type = request.getEmail() != null ? "email" : "phone";
        String target = request.getEmail() != null ? request.getEmail() : request.getPhone();

        if (!verificationCodeService.verifyCode(target, request.getCode(), "register")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查用户是否已存在
        if (request.getEmail() != null) {
            var existingUser = userMapper.selectListByQuery(
                    QueryWrapper.create()
                            .select()
                            .from(USER)
                            .where(USER.EMAIL.eq(request.getEmail()))
            );
            if (existingUser != null && !existingUser.isEmpty()) {
                throw new RuntimeException("该邮箱已被注册");
            }
        }

        if (request.getPhone() != null) {
            var existingUser = userMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(User::getPhone).eq(request.getPhone())
            );
            if (existingUser != null && !existingUser.isEmpty()) {
                throw new RuntimeException("该手机号已被注册");
            }
        }

        // 创建用户
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status("active")
                .emailVerified(request.getEmail() != null)
                .phoneVerified(request.getPhone() != null)
                .build();

        userMapper.insert(user);

        // 记录审计日志
        saveAuditLog(user.getUserId(), "register", ipAddress, userAgent, true, null);

        // 生成 Token
        return generateTokens(user, ipAddress, userAgent);
    }

    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        
        User user = new User();

        // 根据登录方式查找用户
        if (request.getEmail() != null) {
            var users = userMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(User::getEmail).eq(request.getEmail())
            );
            if (users != null && users.isEmpty()) {
                saveAuditLog(null, "login", ipAddress, userAgent, false, "用户不存在");
                throw new RuntimeException("用户名或密码错误");
            }
            if (users != null) {
                user = users.getFirst();
            }
        } else if (request.getPhone() != null) {
            var users = userMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(User::getPhone).eq(request.getPhone())
            );
            if (users != null && users.isEmpty()) {
                saveAuditLog(null, "login", ipAddress, userAgent, false, "用户不存在");
                throw new RuntimeException("用户名或密码错误");
            }
            if (users != null) {
                user = users.getFirst();
            }
        } else {
            throw new RuntimeException("请提供邮箱或手机号");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveAuditLog(user.getUserId(), "login", ipAddress, userAgent, false, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            saveAuditLog(user.getUserId(), "login", ipAddress, userAgent, false, "用户状态异常");
            throw new RuntimeException("账户已被禁用");
        }

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userMapper.update(user);

        // 记录审计日志
        saveAuditLog(user.getUserId(), "login", ipAddress, userAgent, true, null);

        // 生成 Token
        return generateTokens(user, ipAddress, userAgent);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        // 验证 refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("无效的刷新令牌类型");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 从 Redis 获取用户信息
        String cachedUserId = tokenRepository.getUserIdByRefreshToken(refreshToken);
        if (cachedUserId == null || !cachedUserId.equals(userId)) {
            throw new RuntimeException("刷新令牌已失效");
        }

        // 查询用户
        var users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(User::getUserId).eq(userId)
        );

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = users.getFirst();

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
        String type = request.getEmail() != null ? "email" : "phone";
        String target = request.getEmail() != null ? request.getEmail() : request.getPhone();

        // 验证验证码
        if (!verificationCodeService.verifyCode(target, request.getCode(), "reset_password")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 查找用户
        var users = userMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(type.equals("email") ? User::getEmail : User::getPhone).eq(target)
        );

        if (users != null && users.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        User user = null;
        if (users != null) {
            user = users.getFirst();
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userMapper.update(user);
        }
    }

    /**
     * 生成 Token
     */
    private AuthResponse generateTokens(User user, String ipAddress, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "free" // 默认计划
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        String idToken = jwtUtil.generateIdToken(
                user.getUserId(),
                user.getEmail(),
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "free"
        );

        // 存储 Token 到 Redis
        tokenRepository.saveAccessToken(user.getUserId(), accessToken, jwtProperties.getAccessTokenExpiration() / 1000);
        tokenRepository.saveRefreshToken(user.getUserId(), refreshToken, jwtProperties.getRefreshTokenExpiration() / 1000);

        // 保存 Refresh Token 到数据库
        RefreshToken token = RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
                .build();
        refreshTokenMapper.insert(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .idToken(idToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .user(UserInfo.builder()
                        .id(user.getUserId())
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
    private void saveAuditLog(String userId, String action, String ipAddress,
                              String userAgent, boolean success, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .details(details)
                .build();
        auditLogMapper.insert(auditLog);
    }
}
