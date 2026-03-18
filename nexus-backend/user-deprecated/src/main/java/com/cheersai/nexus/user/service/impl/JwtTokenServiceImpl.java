package com.cheersai.nexus.user.service.impl;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.user.service.JwtTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ==============================================================
 * JWT Token服务实现类 - 负责JWT Token的生成与验证
 * 
 * 职责说明：
 * - 使用jjwt库生成和验证JWT Token
 * - 实现三种Token的生成逻辑
 * - 实现Token验证和解析
 * 
 * 三种Token说明：
 * 1. Access Token（访问令牌）
 *    - 用途：API访问授权
 *    - 有效期：1小时（配置：jwt.access-token-expiration）
 *    - 包含：userId、username
 * 
 * 2. Refresh Token（刷新令牌）
 *    - 用途：刷新Access Token
 *    - 有效期：7天（配置：jwt.refresh-token-expiration）
 *    - 包含：userId
 *    - 特点：一次性使用，每次刷新后生成新的
 * 
 * 3. ID Token（身份令牌）
 *    - 用途：身份信息传递
 *    - 有效期：30天（配置：jwt.id-token-expiration）
 *    - 包含：userId、username、email、phone、nickname等
 *    - 特点：包含用户基本信息，可用于前端展示
 * 
 * 安全说明：
 * - 使用HS256算法签名
 * - 密钥通过配置注入（jwt.secret）
 * - Token包含类型标识，防止混淆使用
 * - 使用jjwt 0.12.x版本
 * ==============================================================
 */
@Slf4j
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    // ==================== 配置注入 ====================
    
    /** JWT签名密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** Access Token有效期（毫秒） */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /** Refresh Token有效期（毫秒） */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /** ID Token有效期（毫秒） */
    @Value("${jwt.id-token-expiration}")
    private long idTokenExpiration;

    // ==================== 常量定义 ====================
    
    /** Token类型：访问令牌 */
    private static final String TOKEN_TYPE_ACCESS = "access";
    /** Token类型：刷新令牌 */
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    /** Token类型：身份令牌 */
    private static final String TOKEN_TYPE_ID = "id";

    // ==================== 私有方法 ====================
    
    /**
     * ==============================================================
     * 获取签名密钥
     * 
     * 功能说明：
     * - 根据配置的密钥字符串生成HMAC签名密钥
     * - 使用HS256算法，要求密钥长度足够长
     * 
     * 返回：SecretKey对象
     * ==============================================================
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== Token生成 ====================
    
    /**
     * ==============================================================
     * 生成Access Token（访问令牌）
     * 
     * 功能说明：
     * - 生成用于API访问的短期令牌
     * - 有效期通常较短（1小时），安全性较高
     * 
     * Token内容（Claims）：
     * - type: "access"（令牌类型标识）
     * - userId: 用户唯一标识（subject）
     * - username: 用户名
     * - iat: 签发时间
     * - exp: 过期时间
     * 
     * 参数：
     * - userId: 用户唯一标识
     * - username: 用户名
     * 
     * 返回：JWT字符串
     * ==============================================================
     */
    @Override
    public String generateAccessToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TOKEN_TYPE_ACCESS);
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * ==============================================================
     * 生成Refresh Token（刷新令牌）
     * 
     * 功能说明：
     * - 生成用于刷新Access Token的长期令牌
     * - 有效期较长（7天），一次性使用
     * - 刷新后旧Token自动失效
     * 
     * Token内容（Claims）：
     * - type: "refresh"（令牌类型标识）
     * - userId: 用户唯一标识（subject）
     * - iat: 签发时间
     * - exp: 过期时间
     * 
     * 参数：
     * - userId: 用户唯一标识
     * 
     * 返回：JWT字符串
     * ==============================================================
     */
    @Override
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TOKEN_TYPE_REFRESH);
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * ==============================================================
     * 生成ID Token（身份令牌）
     * 
     * 功能说明：
     * - 生成包含用户身份信息的令牌
     * - 有效期最长（30天），可用于前端缓存用户信息
     * - 包含完整的用户资料，减少数据库查询
     * 
     * Token内容（Claims）：
     * - type: "id"（令牌类型标识）
     * - userId: 用户唯一标识（subject）
     * - username: 用户名
     * - email: 邮箱地址
     * - phone: 手机号码
     * - nickname: 昵称
     * - avatarUrl: 头像URL
     * - status: 用户状态
     * - emailVerified: 邮箱是否验证
     * - phoneVerified: 手机是否验证
     * - iat: 签发时间
     * - exp: 过期时间
     * 
     * 参数：
     * - user: 用户实体对象
     * 
     * 返回：JWT字符串
     * ==============================================================
     */
    @Override
    public String generateIdToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TOKEN_TYPE_ID);
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("phone", user.getPhone());
        claims.put("nickname", user.getNickname());
        claims.put("avatarUrl", user.getAvatarUrl());
        claims.put("status", user.getStatus());
        claims.put("emailVerified", user.getEmailVerified());
        claims.put("phoneVerified", user.getPhoneVerified());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + idTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ==================== Token验证 ====================
    
    /**
     * ==============================================================
     * 验证Access Token
     * 
     * 功能说明：
     * - 验证Token是否为有效的Access Token
     * - 检查签名、过期时间、Token类型
     * 
     * 参数：
     * - token: JWT字符串
     * 
     * 返回：boolean（true=有效，false=无效）
     * ==============================================================
     */
    @Override
    public boolean validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    /**
     * ==============================================================
     * 验证Refresh Token
     * 
     * 功能说明：
     * - 验证Token是否为有效的Refresh Token
     * - 检查签名、过期时间、Token类型
     * 
     * 参数：
     * - token: JWT字符串
     * 
     * 返回：boolean（true=有效，false=无效）
     * ==============================================================
     */
    @Override
    public boolean validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    /**
     * ==============================================================
     * Token验证核心方法
     * 
     * 功能说明：
     * - 统一的Token验证逻辑
     * - 验证签名是否正确
     * - 验证Token类型是否匹配
     * - 验证是否过期
     * 
     * 参数：
     * - token: JWT字符串
     * - expectedType: 期望的Token类型
     * 
     * 返回：boolean（true=有效，false=无效）
     * 
     * 异常处理：
     * - ExpiredJwtException: Token已过期
     * - MalformedJwtException: Token格式错误
     * - SignatureException: 签名验证失败
     * - UnsupportedJwtException: 不支持的Token
     * - IllegalArgumentException: Token参数无效
     * ==============================================================
     */
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return expectedType.equals(type);
        } catch (JwtException e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ==============================================================
     * 从Token中获取用户ID
     * 
     * 功能说明：
     * - 从任意类型的Token中提取用户ID
     * - 用户ID存储在Token的subject中
     * 
     * 参数：
     * - token: JWT字符串
     * 
     * 返回：用户ID（提取失败返回null）
     * ==============================================================
     */
    @Override
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException e) {
            log.debug("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 配置获取 ====================
    
    /**
     * 获取Access Token过期时间（毫秒）
     */
    @Override
    public long getAccessTokenExpireIn() {
        return accessTokenExpiration;
    }

    /**
     * 获取Refresh Token过期时间（毫秒）
     */
    @Override
    public long getRefreshTokenExpireIn() {
        return refreshTokenExpiration;
    }

    /**
     * 获取ID Token过期时间（毫秒）
     */
    @Override
    public long getIdTokenExpireIn() {
        return idTokenExpiration;
    }
}
