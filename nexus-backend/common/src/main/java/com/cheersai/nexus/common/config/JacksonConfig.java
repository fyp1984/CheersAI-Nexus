package com.cheersai.nexus.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 全局Jackson配置类，统一序列化/反序列化规则，解决空值、日期格式化等问题
 */

@Configuration
public class JacksonConfig {

    /**
     * 自定义ObjectMapper，设置为默认实例（@Primary）
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. 序列化配置：空值不序列化（可根据业务调整为JsonInclude.Include.ALWAYS）
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 2. 关闭不必要的序列化特性，提升性能
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 3. 反序列化配置：忽略未知字段（避免前端传多余字段导致异常）
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 4. 日期格式化：统一处理Date和LocalDateTime/JSR310时间类型
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(sdf);
        objectMapper.registerModule(new JavaTimeModule()); // 支持JDK8+时间类型
        // 5. 时区配置：统一使用东八区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 注意：移除 activateDefaultTyping，因为这对普通 REST API 有害
        // 如果需要多态类型，应该在特定 DTO 上使用 @JsonTypeInfo 注解
        return objectMapper;
    }
}