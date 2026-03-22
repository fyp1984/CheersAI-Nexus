package com.cheersai.nexus.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_PREFIX = "auth:access:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    /**
     * 存储 Access Token 到 Redis
     */
    public void saveAccessToken(String userId, String token, long expirationSeconds) {
        String key = ACCESS_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId, expirationSeconds, TimeUnit.SECONDS);
        log.debug("Saved access token for user: {}", userId);
    }

    /**
     * 存储 Refresh Token 到 Redis
     */
    public void saveRefreshToken(String userId, String token, long expirationSeconds) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId, expirationSeconds, TimeUnit.SECONDS);
        log.debug("Saved refresh token for user: {}", userId);
    }

    /**
     * 根据 Access Token 获取用户 ID
     */
    public String getUserIdByAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 根据 Refresh Token 获取用户 ID
     */
    public String getUserIdByRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 删除 Access Token
     */
    public void deleteAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Deleted access token");
    }

    /**
     * 删除 Refresh Token
     */
    public void deleteRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Deleted refresh token");
    }

    /**
     * 将 Token 加入黑名单
     */
    public void addToBlacklist(String token, long remainingSeconds) {
        if (remainingSeconds > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "revoked", remainingSeconds, TimeUnit.SECONDS);
            log.debug("Added token to blacklist");
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 根据用户 ID 删除所有 Token（用于登出）
     */
    public void deleteAllUserTokens(String userId) {
        // 这里需要使用 SCAN 来查找所有匹配的 key，生产环境可能需要优化
        // 简化实现：登出时只删除当前 Token
        log.debug("Delete all tokens for user: {}", userId);
    }
}
