package com.cheersai.nexus.feedback.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * 仅 Feedback 模块的 HTTP JSON 转换器配置。
 * 目的：避免 common 中 default typing 对请求体反序列化造成影响，从而导致请求体格式转换错误。
 */
@Configuration
public class FeedbackWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper httpMapper = new ObjectMapper();
        httpMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        httpMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        httpMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        httpMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        httpMapper.registerModule(new JavaTimeModule());
        httpMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setObjectMapper(httpMapper);
            }
        }
    }
}
