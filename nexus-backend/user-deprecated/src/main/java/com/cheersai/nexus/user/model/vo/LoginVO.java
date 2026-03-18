package com.cheersai.nexus.user.model.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    /**
     * Access Token
     */
    private String accessToken;

    /**
     * Access Token过期时间（秒）
     */
    private Long accessTokenExpireIn;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Refresh Token过期时间（秒）
     */
    private Long refreshTokenExpireIn;

    /**
     * ID Token
     */
    private String idToken;

    /**
     * ID Token过期时间（秒）
     */
    private Long idTokenExpireIn;

    /**
     * 用户信息
     */
    private UserVO user;
}
