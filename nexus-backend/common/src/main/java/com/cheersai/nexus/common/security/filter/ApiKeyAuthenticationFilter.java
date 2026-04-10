package com.cheersai.nexus.common.security.filter;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.common.security.entity.ApiKey;
import com.cheersai.nexus.common.security.exception.ApiKeyAuthenticationException;
import com.cheersai.nexus.common.security.service.ApiKeyService;
import com.cheersai.nexus.common.security.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

/**
 * API Key 认证过滤器
 * 用于验证外部客户端请求（X-API-Key / X-API-Secret）
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService, RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.apiKeyService = apiKeyService;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/external/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String keyId = request.getHeader("X-API-Key");
        String keySecret = request.getHeader("X-API-Secret");

        if (keyId == null || keySecret == null) {
            writeError(response, 401, "缺少API凭证(X-API-Key / X-API-Secret)");
            return;
        }

        try {
            ApiKey apiKey = apiKeyService.validate(keyId, keySecret)
                    .orElseThrow(() -> new ApiKeyAuthenticationException("无效的API凭证"));

            if (!rateLimitService.tryConsume(apiKey.getKeyId(), apiKey.getRateLimit())) {
                writeError(response, 429, "请求频率超限，请稍后重试");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    apiKey, null,
                    List.of(new SimpleGrantedAuthority("ROLE_EXTERNAL_CLIENT"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (ApiKeyAuthenticationException e) {
            writeError(response, 401, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
