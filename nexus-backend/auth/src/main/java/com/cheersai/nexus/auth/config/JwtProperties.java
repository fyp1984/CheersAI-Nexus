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
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private long idTokenExpiration;
}
