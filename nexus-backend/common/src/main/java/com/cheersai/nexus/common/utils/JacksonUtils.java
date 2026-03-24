package com.cheersai.nexus.common.utils;

import com.cheersai.nexus.common.model.base.Input;
import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.common.model.base.SysUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jackson工具类，内置类型缓存，解决重复解析类结构的性能问题
 * 单例注入Spring容器，使用全局配置的ObjectMapper
 */
@Component
public class JacksonUtils {

    // 注入全局配置的ObjectMapper（由JacksonConfig提供）
    private final ObjectMapper objectMapper;

    // 核心：线程安全的类型缓存Map，缓存JavaType，避免重复解析类结构
    private final Map<Class<?>, JavaType> typeCache = new ConcurrentHashMap<>();

    // 构造器注入（Spring官方推荐，避免字段注入的循环依赖问题）
    @Autowired
    public JacksonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // 预缓存业务中高频使用的类（核心优化点），项目启动时初始化，运行时直接使用
        preCacheType(Input.class);
        preCacheType(Result.class);
        preCacheType(SysUser.class);
        preCacheType(Map.class);
    }

    /**
     * 预缓存类的JavaType，私有化方法，仅初始化时调用
     */
    private void preCacheType(Class<?> clazz) {
        if (!typeCache.containsKey(clazz)) {
            JavaType javaType = objectMapper.constructType(clazz);
            typeCache.put(clazz, javaType);
        }
    }

    /**
     * 序列化：对象转JSON字符串
     *
     * @param obj 待序列化对象
     * @return JSON字符串
     * @throws JsonProcessingException 序列化异常
     */
    public String toJson(Object obj) throws JsonProcessingException {
        if (obj == null) {
            return null;
        }
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 反序列化：JSON字符串转指定类对象（使用缓存的JavaType，核心性能优化）
     *
     * @param json  JSON字符串
     * @param clazz 目标类
     * @return 目标类实例
     * @throws JsonProcessingException 反序列化异常
     */
    public <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        if (json == null || json.isBlank() || clazz == null) {
            return null;
        }
        // 从缓存获取JavaType，无则创建并加入缓存（懒加载，兼容未预缓存的类）
        JavaType javaType = typeCache.computeIfAbsent(clazz, objectMapper::constructType);
        return objectMapper.readValue(json, javaType);
    }
}