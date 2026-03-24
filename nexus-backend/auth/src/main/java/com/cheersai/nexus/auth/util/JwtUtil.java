package com.cheersai.nexus.auth.util;


import com.cheersai.nexus.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Autowired
    private final JwtProperties jwtProperties;
    
    private RSAPrivateKey privateKey;

    /**
     * -- GETTER --
     *  获取公钥（用于提供给前端）
     */
    @Getter
    private RSAPublicKey publicKey;

    /**
     * 初始化 RSA 密钥对
     */
    @PostConstruct
    public void initKeyPair() {
        try {
            if (jwtProperties.getPrivateKey() != null && !jwtProperties.getPrivateKey().isEmpty()
                    && jwtProperties.getPublicKey() != null && !jwtProperties.getPublicKey().isEmpty()) {
                // 使用配置中的密钥
                privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
                publicKey = loadPublicKey(jwtProperties.getPublicKey());
                log.info("Loaded RSA keys from configuration");
            } else {
                // 生成新的密钥对（仅用于开发环境）
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                privateKey = (RSAPrivateKey) keyPair.getPrivate();
                publicKey = (RSAPublicKey) keyPair.getPublic();
                log.warn("Generated new RSA key pair - USE IN DEVELOPMENT ONLY!");
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to initialize RSA key pair", e);
            throw new RuntimeException("Failed to initialize RSA key pair", e);
        }
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(String userId, String email, String name, String plan) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        claims.put("plan", plan);
        claims.put("type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .claim("type", "refresh")
                .subject(String.valueOf(userId))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 生成 ID Token
     */
    public String generateIdToken(String userId, String email, String name, String plan) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        claims.put("plan", plan);
        claims.put("type", "id");

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getIdTokenExpiration()))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中提取用户 ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中提取 token 类型
     */
    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    /**
     * 加载私钥
     */
    private RSAPrivateKey loadPrivateKey(String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Failed to load private key", e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    /**
     * 加载公钥
     */
    private RSAPublicKey loadPublicKey(String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Failed to load public key", e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    /**
     * 获取公钥的 Base64 编码
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
