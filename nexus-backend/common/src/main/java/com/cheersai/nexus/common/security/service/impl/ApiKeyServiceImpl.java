package com.cheersai.nexus.common.security.service.impl;

import com.cheersai.nexus.common.security.entity.ApiKey;
import com.cheersai.nexus.common.security.mapper.ApiKeyMapper;
import com.cheersai.nexus.common.security.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Optional<ApiKey> validate(String keyId, String keySecret) {
        // Query by keyId using selectOneByQuery with raw column conditions
        ApiKey apiKey = apiKeyMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where("key_id = #{keyId}")
                        .and("status = 'active'")
        );

        if (apiKey == null) {
            return Optional.empty();
        }

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        if (!passwordEncoder.matches(keySecret, apiKey.getKeySecretHash())) {
            return Optional.empty();
        }

        // Update last_used_at
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyMapper.update(apiKey);

        return Optional.of(apiKey);
    }

    @Override
    @Transactional
    public ApiKey create(String name, UUID createdBy) {
        String plainKeyId = UUID.randomUUID().toString().replace("-", "");
        String plainKeySecret = UUID.randomUUID().toString().replace("-", "");

        ApiKey apiKey = ApiKey.builder()
                .keyId(plainKeyId)
                .keySecretHash(passwordEncoder.encode(plainKeySecret))
                .name(name)
                .status("active")
                .rateLimit(1000)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        apiKeyMapper.insert(apiKey);

        // Return with plain secret for one-time display
        apiKey.setKeySecretHash(plainKeySecret);
        return apiKey;
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        ApiKey apiKey = apiKeyMapper.selectOneById(id);
        if (apiKey != null) {
            apiKey.setStatus("revoked");
            apiKeyMapper.update(apiKey);
        }
    }

    @Override
    public List<ApiKey> listAll() {
        return apiKeyMapper.selectAll();
    }
}
