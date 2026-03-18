package com.cheersai.nexus.auth.service;

public interface VerificationCodeService {
    String generateCode();
    void sendVerificationCode(String target, String type, String purpose);
    boolean verifyCode(String target, String code, String purpose);
}
