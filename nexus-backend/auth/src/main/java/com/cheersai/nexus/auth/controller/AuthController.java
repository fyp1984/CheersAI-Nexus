package com.cheersai.nexus.auth.controller;


import com.cheersai.nexus.auth.dto.*;
import com.cheersai.nexus.auth.service.AuthService;
import com.cheersai.nexus.auth.service.VerificationCodeService;
import com.cheersai.nexus.common.model.base.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;
    
    @Autowired
    private final VerificationCodeService verificationCodeService;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String target = request.getEmail() != null ? request.getEmail() : request.getPhone();
        String type = request.getEmail() != null ? "email" : "phone";

        verificationCodeService.sendVerificationCode(target, type, request.getPurpose());
        return Result.success();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                         HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.register(request, ipAddress, userAgent);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, ipAddress, userAgent);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String accessToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getPrincipal().toString() : null;

        // 从请求头获取 refresh token
        String refreshToken = httpRequest.getHeader("X-Refresh-Token");

        authService.logout(accessToken, refreshToken, userId);
        return Result.success();
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@RequestHeader("X-Refresh-Token") String refreshToken,
                                         HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.refreshToken(refreshToken, ipAddress, userAgent);
        return Result.success(response);
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getPrincipal().toString();
        return Result.success(authService.getCurrentUser(userId));
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
