package com.cheersai.nexus.user.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

public class TimeoutRequestFactory extends SimpleClientHttpRequestFactory {

    public TimeoutRequestFactory(Duration timeout) {
        int timeoutMillis = (int) Math.max(1000, timeout.toMillis());
        setConnectTimeout(timeoutMillis);
        setReadTimeout(timeoutMillis);
    }
}
