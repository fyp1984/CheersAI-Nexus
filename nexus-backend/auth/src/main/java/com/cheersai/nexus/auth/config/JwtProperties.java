package com.cheersai.nexus.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String issuer;
    private String audience;
    private String privateKey;
    private String publicKey;
    // Access Token：2 小时，API 访问凭证（单位：毫秒）
    private long accessTokenExpiration = 2 * 60 * 60 * 1000L;

    // Refresh Token：7 天，刷新 Access Token
    private long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000L;

    // ID Token：2 小时，用户身份信息
    private long idTokenExpiration = 2 * 60 * 60 * 1000L;
}
