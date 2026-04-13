package com.cheersai.nexus.user.service;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.user.config.BetaNotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetaNotificationService {

    private static final String NOTIFICATION_PATH = "/inner/api/enterprise/beta-notifications";
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    private final BetaNotificationProperties properties;

    public void notifySubmitted(User user, String language) {
        sendEvent(
                "submitted",
                user,
                language,
                payload -> {
                }
        );
    }

    public void notifyApproved(User user, String ssoUsername, String filebayRepo, String ssoInitialPassword) {
        sendEvent(
                "approved",
                user,
                properties.getDefaultLanguage(),
                payload -> {
                    payload.put("sso_username", ssoUsername);
                    payload.put("filebay_repo", filebayRepo);
                    if (StringUtils.hasText(ssoInitialPassword)) {
                        payload.put("sso_initial_password", ssoInitialPassword);
                    }
                }
        );
    }

    public void notifyRejected(User user, String reason) {
        String resolvedReason = StringUtils.hasText(reason) ? reason.trim() : properties.getRejectedReason();
        sendEvent(
                "rejected",
                user,
                properties.getDefaultLanguage(),
                payload -> payload.put("reason", resolvedReason)
        );
    }

    public void notifyProvisionFailed(User user, String errorMessage) {
        String resolvedErrorMessage = StringUtils.hasText(errorMessage)
                ? errorMessage.trim()
                : "系统开通过程中发生未知异常，请联系管理员处理。";
        if (resolvedErrorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
            resolvedErrorMessage = resolvedErrorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
        }
        String safeResolvedErrorMessage = resolvedErrorMessage;
        sendEvent(
                "provision_failed",
                user,
                properties.getDefaultLanguage(),
                payload -> payload.put("error_message", safeResolvedErrorMessage)
        );
    }

    private void sendEvent(String event, User user, String language, PayloadCustomizer customizer) {
        if (!properties.isEnabled()) {
            return;
        }
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            log.warn("Skip beta notification {} because user/email is missing", event);
            return;
        }
        if (!StringUtils.hasText(properties.getDesktopApiBaseUrl()) || !StringUtils.hasText(properties.getInnerApiKey())) {
            log.warn("Skip beta notification {} because Desktop internal notification config is incomplete", event);
            return;
        }

        String normalizedBaseUrl = properties.getDesktopApiBaseUrl().trim().replaceAll("/+$", "");
        String resolvedLanguage = StringUtils.hasText(language) ? language.trim() : properties.getDefaultLanguage();

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("to", user.getEmail().trim().toLowerCase(Locale.ROOT));
        payload.put("name", user.getNickname());
        payload.put("language", resolvedLanguage);
        customizer.customize(payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-Inner-Api-Key", properties.getInnerApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new TimeoutRequestFactory(Duration.ofSeconds(properties.getHttpTimeoutSeconds())));

        String url = UriComponentsBuilder.fromHttpUrl(normalizedBaseUrl + NOTIFICATION_PATH)
                .build(true)
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Desktop beta notification {} failed with status {}", event, response.getStatusCode().value());
            }
        } catch (Exception ex) {
            log.warn("Desktop beta notification {} request failed: {}", event, ex.getMessage());
        }
    }

    @FunctionalInterface
    private interface PayloadCustomizer {
        void customize(Map<String, Object> payload);
    }
}
