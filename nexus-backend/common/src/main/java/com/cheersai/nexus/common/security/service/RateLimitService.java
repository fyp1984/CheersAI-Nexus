package com.cheersai.nexus.common.security.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存级令牌桶限流服务
 */
@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String keyId, int maxRequestsPerMinute) {
        TokenBucket bucket = buckets.computeIfAbsent(keyId, k -> new TokenBucket(maxRequestsPerMinute));
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private final int maxTokens;
        private int tokens;
        private long lastRefillTime;

        TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= 60_000) {
                tokens = maxTokens;
                lastRefillTime = now;
            }
        }
    }
}
