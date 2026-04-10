package com.cheersai.nexus.feedback.config;

import com.cheersai.nexus.common.model.base.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 内部服务间调用认证过滤器
 * 通过 X-Internal-Secret header 验证调用方身份
 */
public class InternalSecretAuthenticationFilter extends OncePerRequestFilter {

    private static final String SECRET_HEADER = "X-Internal-Secret";

    private final String expectedSecret;
    private final ObjectMapper objectMapper;

    public InternalSecretAuthenticationFilter(String expectedSecret, ObjectMapper objectMapper) {
        this.expectedSecret = expectedSecret;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String providedSecret = request.getHeader(SECRET_HEADER);

        if (providedSecret == null || !providedSecret.equals(expectedSecret)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(Result.error(401, "Invalid or missing internal secret"))
            );
            return;
        }

        // Create authentication with INTERNAL_SERVICE role
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "internal-service", null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
