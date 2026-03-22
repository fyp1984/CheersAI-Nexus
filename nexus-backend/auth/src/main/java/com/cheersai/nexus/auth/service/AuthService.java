package com.cheersai.nexus.auth.service;

import com.cheersai.nexus.auth.dto.AuthResponse;
import com.cheersai.nexus.auth.dto.LoginRequest;
import com.cheersai.nexus.auth.dto.RegisterRequest;
import com.cheersai.nexus.auth.dto.ResetPasswordRequest;
import com.cheersai.nexus.common.model.usermanagement.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress, String userAgent);
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);
    AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent);
    void logout(String accessToken, String refreshToken, String userId);
    void resetPassword(ResetPasswordRequest request);
}
