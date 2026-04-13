package com.cheersai.nexus.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "beta.notification")
public class BetaNotificationProperties {

    private boolean enabled = true;

    private int httpTimeoutSeconds = 10;

    private String desktopApiBaseUrl;

    private String innerApiKey;

    private String defaultLanguage = "zh-Hans";

    private String rejectedReason = "管理员审核未通过，如需帮助请联系支持人员。";
}
