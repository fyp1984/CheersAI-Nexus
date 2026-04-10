package com.cheersai.nexus.common.security.filter;

import com.cheersai.nexus.common.model.base.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * JWT 认证过滤器
 * 使用 RSA 公钥验证 JWT access token
 */
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {

    private final RSAPublicKey publicKey;
    private final ObjectMapper objectMapper;
    private final List<String> protectedPaths;

    public JwtValidationFilter(String publicKeyBase64, ObjectMapper objectMapper, List<String> protectedPaths) {
        this.objectMapper = objectMapper;
        this.publicKey = loadPublicKey(publicKeyBase64);
        this.protectedPaths = protectedPaths != null ? protectedPaths : List.of();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return protectedPaths.stream().noneMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "缺少认证令牌");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Only allow access tokens
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                writeError(response, 401, "无效的令牌类型");
                return;
            }

            String userId = claims.getSubject();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            writeError(response, 401, "无效或过期的认证令牌");
        }
    }

    private RSAPublicKey loadPublicKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA public key for JWT validation", e);
        }
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
