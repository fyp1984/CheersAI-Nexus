package com.cheersai.nexus.user.service;

import com.cheersai.nexus.common.model.usermanagement.User;

/**
 * JWT Token服务接口
 */
public interface JwtTokenService {

    /**
     * 生成Access Token
     */
    String generateAccessToken(String userId, String username);

    /**
     * 生成Refresh Token
     */
    String generateRefreshToken(String userId);

    /**
     * 生成ID Token
     */
    String generateIdToken(User user);

    /**
     * 验证Access Token
     */
    boolean validateAccessToken(String token);

    /**
     * 验证Refresh Token
     */
    boolean validateRefreshToken(String token);

    /**
     * 从Token中获取用户ID
     */
    String getUserIdFromToken(String token);

    /**
     * 获取Access Token过期时间（毫秒）
     */
    long getAccessTokenExpireIn();

    /**
     * 获取Refresh Token过期时间（毫秒）
     */
    long getRefreshTokenExpireIn();

    /**
     * 获取ID Token过期时间（毫秒）
     */
    long getIdTokenExpireIn();
}
