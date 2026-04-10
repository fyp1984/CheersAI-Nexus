package com.cheersai.nexus.membership.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client 配置
 * 为内部服务间调用添加共享密钥 header
 */
@Configuration
public class FeedbackClientConfig implements RequestInterceptor {

    private static final String SECRET_HEADER = "X-Internal-Secret";

    @Value("${internal.secret:${INTERNAL_SECRET:cheersai-internal-secret-2024}}")
    private String internalSecret;

    @Override
    public void apply(RequestTemplate template) {
        template.header(SECRET_HEADER, internalSecret);
    }
}
