package com.cheersai.nexus.user.service;

import com.cheersai.nexus.user.config.BetaProvisionProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BetaProvisioningServiceTest {

    @Test
    void defaultPropertiesShouldUseMemberRoleAndPersonalRepoMode() {
        BetaProvisionProperties properties = new BetaProvisionProperties();

        assertEquals("desktop_team_member", properties.getSsoDefaultRole());
        assertEquals("CheersAI/系统内测", properties.getSsoDefaultGroup());
        assertEquals(BetaProvisionProperties.FilebayProvisionMode.PERSONAL_REPO, properties.getFilebayMode());
    }

    @Test
    void buildFilebayOrgNameShouldPrefixAndNormalizeUsername() {
        BetaProvisionProperties properties = new BetaProvisionProperties();
        properties.setFilebayMode(BetaProvisionProperties.FilebayProvisionMode.ORG_REPO);
        properties.setFilebayOrgPrefix("desktop");
        BetaProvisioningService service = new BetaProvisioningService(properties, new ObjectMapper());

        assertEquals("desktop_e2e_beta_20260420_user", service.buildFilebayOrgName("E2E Beta@20260420 User"));
    }

    @Test
    void buildFilebayOrgNameShouldFallbackWhenPrefixIsBlank() {
        BetaProvisionProperties properties = new BetaProvisionProperties();
        properties.setFilebayMode(BetaProvisionProperties.FilebayProvisionMode.ORG_REPO);
        properties.setFilebayOrgPrefix(" ");
        BetaProvisioningService service = new BetaProvisioningService(properties, new ObjectMapper());

        assertEquals("org_demo_user", service.buildFilebayOrgName("demo_user"));
    }
}
