package com.cheersai.nexus.auth.service.impl;


import com.cheersai.nexus.auth.entity.VerificationCode;
import com.cheersai.nexus.auth.mapper.VerificationCodeMapper;
import com.cheersai.nexus.auth.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.verification.code-length:6}")
    private int codeLength;

    @Value("${app.verification.code-expiration:300000}")
    private long codeExpiration; // 5 minutes

    private static final String VERIFICATION_CODE_RATE_LIMIT_PREFIX = "verification:ratelimit:";

    /**
     * 生成验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送验证码（邮箱或手机）
     * 实际项目中需要集成邮件服务或短信服务
     */
    public void sendVerificationCode(String target, String type, String purpose) {
        // 检查频率限制
        String rateLimitKey = VERIFICATION_CODE_RATE_LIMIT_PREFIX + target;
        Boolean isLimited = redisTemplate.hasKey(rateLimitKey);
        if (isLimited) {
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }

        // 生成验证码
        String code = generateCode();

        // 保存验证码到数据库
        VerificationCode verificationCode = VerificationCode.builder()
                .target(target)
                .code(code)
                .type(type)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(codeExpiration)))
                .used(false)
                .build();
        verificationCodeMapper.insert(verificationCode);

        // 设置频率限制
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS); // 60秒内只能发送一次

        // 实际发送（开发环境打印到日志）
        if ("email".equals(type)) {
            log.info("【CheersAI】您的验证码是：{}，5分钟内有效。", code);
            // TODO: 集成邮件服务发送验证码
        } else if ("phone".equals(type)) {
            log.info("【CheersAI】您的验证码是：{}，5分钟内有效。", code);
            // TODO: 集成短信服务发送验证码
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String target, String code, String purpose) {
        // 先从 Redis 验证（快速失败）
        String redisKey = "verification:" + target + ":" + purpose;
        String cachedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证成功，删除 Redis 中的验证码
            redisTemplate.delete(redisKey);
            // 标记数据库中的验证码为已使用
            markCodeAsUsed(target, purpose);
            return true;
        }

        // 从数据库验证
        var verificationCodes = verificationCodeMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where(VerificationCode::getTarget).eq(target)
                        .and(VerificationCode::getPurpose).eq(purpose)
                        .and(VerificationCode::getUsed).eq(false)
                        .orderBy(VerificationCode::getCreatedAt, false)
                        .limit(1)
        );

        if (verificationCodes.isEmpty()) {
            return false;
        }

        VerificationCode verificationCode = verificationCodes.getFirst();

        // 检查是否过期
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 验证验证码
        if (verificationCode.getCode().equals(code)) {
            // 标记为已使用
            markCodeAsUsed(target, purpose);
            return true;
        }

        return false;
    }

    /**
     * 标记验证码为已使用
     */
    private void markCodeAsUsed(String target, String purpose) {
        var verificationCodes = verificationCodeMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where(VerificationCode::getTarget).eq(target)
                        .and(VerificationCode::getPurpose).eq(purpose)
                        .and(VerificationCode::getUsed).eq(false)
        );

        for (VerificationCode vc : verificationCodes) {
            vc.setUsed(true);
            verificationCodeMapper.update(vc);
        }
    }
}
