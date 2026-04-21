package com.cheersai.nexus.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "beta.provision")
public class BetaProvisionProperties {

    public enum FilebayProvisionMode {
        PERSONAL_REPO,
        ORG_REPO
    }

    private boolean enabled = true;

    private int httpTimeoutSeconds = 15;

    private String ssoBaseUrl;

    private String ssoOwner = "CheersAI";

    private String ssoClientId;

    private String ssoClientSecret;

    private String ssoDefaultRole = "desktop_team_member";

    private String ssoDefaultGroup = "CheersAI/系统内测";

    private String ssoSignupApplication = "desktop";

    private String defaultPassword = "2026@CheersAI";

    private String filebayBaseUrl;

    private String filebayAdminUsername;

    private String filebayAdminPassword;

    private FilebayProvisionMode filebayMode = FilebayProvisionMode.PERSONAL_REPO;

    private String filebayOrgPrefix = "desktop";

    private String filebayOrgVisibility = "private";

    private boolean filebayOrgCreateDefaultTeam = false;

    private String filebayOrgDefaultTeamName = "members";

    private String filebayOrgDefaultTeamPermission = "write";

    private String filebayDefaultRepo = "workspace";

    private String filebayDefaultBranch = "main";

    private String filebayDefaultMaskedDir = "masked";
}
