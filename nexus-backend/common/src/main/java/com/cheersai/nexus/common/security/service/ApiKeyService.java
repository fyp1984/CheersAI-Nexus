package com.cheersai.nexus.common.security.service;

import com.cheersai.nexus.common.security.entity.ApiKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyService {

    Optional<ApiKey> validate(String keyId, String keySecret);

    ApiKey create(String name, UUID createdBy);

    void revoke(UUID id);

    List<ApiKey> listAll();
}
