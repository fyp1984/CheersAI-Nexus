package com.cheersai.nexus.feedback.config;

import com.cheersai.nexus.common.security.filter.ApiKeyAuthenticationFilter;
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

/**
 * Feedback 模块安全配置
 * - 外部端点（/api/v1/external/**）使用 API Key 认证
 * - 内部服务间端点（/api/v1/internal/**）使用共享密钥认证
 * - 内部反馈管理端点不受 Security 限制（保持向后兼容）
 */
@Configuration
@EnableWebSecurity
public class FeedbackSecurityConfig {

    @Value("${internal.secret:${INTERNAL_SECRET:cheersai-internal-secret-2024}}")
    private String internalSecret;

    @Bean
    public InternalSecretAuthenticationFilter internalSecretAuthenticationFilter(ObjectMapper objectMapper) {
        return new InternalSecretAuthenticationFilter(internalSecret, objectMapper);
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
            InternalSecretAuthenticationFilter internalSecretFilter) throws Exception {
        http.securityMatcher("/api/v1/internal/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("INTERNAL_SERVICE"))
                .addFilterBefore(internalSecretFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
