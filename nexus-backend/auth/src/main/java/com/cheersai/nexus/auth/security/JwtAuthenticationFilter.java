package com.cheersai.nexus.auth.security;


import com.cheersai.nexus.auth.repository.TokenRepository;
import com.cheersai.nexus.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private final JwtUtil jwtUtil;
    
    @Autowired
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 如果没有 Authorization header，直接放行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        // 检查 token 是否在黑名单中
        if (tokenRepository.isTokenBlacklisted(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 验证 token
            if (!jwtUtil.validateToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 检查 token 类型（只允许 access token）
            String tokenType = jwtUtil.getTokenType(jwt);
            if (!"access".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从 token 中提取用户信息
            String userId = jwtUtil.getUserIdFromToken(jwt);

            // 设置认证信息
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authenticated user: {}", userId);
            }

        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
