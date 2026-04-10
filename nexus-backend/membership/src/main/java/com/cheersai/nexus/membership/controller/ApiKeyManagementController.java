package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.common.security.entity.ApiKey;
import com.cheersai.nexus.common.security.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyManagementController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public Result<Map<String, String>> createApiKey(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return Result.error(400, "名称不能为空");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : null;
        UUID createdBy = userId != null ? UUID.fromString(userId) : null;

        ApiKey apiKey = apiKeyService.create(name, createdBy);

        Map<String, String> result = new HashMap<>();
        result.put("keyId", apiKey.getKeyId());
        result.put("keySecret", apiKey.getKeySecretHash()); // plain secret, only returned once
        return Result.success(result);
    }

    @GetMapping
    public Result<List<Map<String, Object>>> listApiKeys() {
        List<ApiKey> keys = apiKeyService.listAll();
        List<Map<String, Object>> result = keys.stream().map(k -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", k.getId() != null ? k.getId().toString() : null);
            map.put("keyId", k.getKeyId());
            map.put("name", k.getName());
            map.put("status", k.getStatus());
            map.put("rateLimit", k.getRateLimit());
            map.put("createdAt", k.getCreatedAt());
            map.put("expiresAt", k.getExpiresAt());
            map.put("lastUsedAt", k.getLastUsedAt());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/{id}/revoke")
    public Result<Void> revokeApiKey(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return Result.success();
    }
}
