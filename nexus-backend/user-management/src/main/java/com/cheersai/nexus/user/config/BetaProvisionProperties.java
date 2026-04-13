package com.cheersai.nexus.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "beta.provision")
public class BetaProvisionProperties {

    private boolean enabled = true;

    private int httpTimeoutSeconds = 15;

    private String ssoBaseUrl;

    private String ssoOwner = "CheersAI";

    private String ssoClientId;

    private String ssoClientSecret;

    private String ssoDefaultRole = "desktop_team_admin";

    private String ssoSignupApplication = "desktop";

    private String filebayBaseUrl;

    private String filebayAdminUsername;

    private String filebayAdminPassword;

    private String filebayDefaultRepo = "workspace";

    private String filebayDefaultBranch = "main";

    private String filebayDefaultMaskedDir = "masked";
}
