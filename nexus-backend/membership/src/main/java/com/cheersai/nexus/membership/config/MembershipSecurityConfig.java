package com.cheersai.nexus.membership.config;

import com.cheersai.nexus.common.security.filter.ApiKeyAuthenticationFilter;
import com.cheersai.nexus.common.security.filter.JwtValidationFilter;
import com.cheersai.nexus.common.security.service.ApiKeyService;
import com.cheersai.nexus.common.security.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;

/**
 * Membership 模块安全配置
 * - 外部端点（/api/v1/external/**）使用 API Key 认证
 * - 内部管理端点（/api/v1/desktop-members/**, /api/v1/api-keys/**）使用 JWT 认证
 * - 内部服务端点（/api/v1/plans/**, /api/v1/subscriptions/**）不受 Security 限制
 */
@Configuration
@EnableWebSecurity
public class MembershipSecurityConfig {

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(
            ApiKeyService apiKeyService,
            RateLimitService rateLimitService,
            ObjectMapper objectMapper) {
        return new ApiKeyAuthenticationFilter(apiKeyService, rateLimitService, objectMapper);
    }

    @Bean
    public JwtValidationFilter jwtValidationFilter(
            @Value("${jwt.public-key:}") String publicKeyBase64,
            ObjectMapper objectMapper) {
        return new JwtValidationFilter(publicKeyBase64, objectMapper,
                List.of("/api/v1/desktop-members/", "/api/v1/api-keys"));
    }

    @Bean
    @Order(1)
    public SecurityFilterChain externalApiFilterChain(
            HttpSecurity http,
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) throws Exception {
        http.securityMatcher("/api/v1/external/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("EXTERNAL_CLIENT"))
                .addFilterBefore(apiKeyAuthenticationFilter, BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain internalApiFilterChain(
            HttpSecurity http,
            JwtValidationFilter jwtValidationFilter) throws Exception {
        http.securityMatcher("/api/v1/desktop-members/**", "/api/v1/api-keys/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(jwtValidationFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
