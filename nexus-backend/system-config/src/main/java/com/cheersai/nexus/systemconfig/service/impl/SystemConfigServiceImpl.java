package com.cheersai.nexus.systemconfig.service.impl;

import com.cheersai.nexus.common.utils.JacksonUtils;
import com.cheersai.nexus.systemconfig.dto.SystemConfigDTO;
import com.cheersai.nexus.systemconfig.entity.IpWhitelist;
import com.cheersai.nexus.systemconfig.entity.SystemConfig;
import com.cheersai.nexus.systemconfig.mapper.IpWhitelistMapper;
import com.cheersai.nexus.systemconfig.mapper.SystemConfigMapper;
import com.cheersai.nexus.systemconfig.service.SystemConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置业务逻辑实现类
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private IpWhitelistMapper ipWhitelistMapper;

    @Autowired
    private JacksonUtils jacksonUtils;

    @Override
    public SystemConfigDTO getAllConfig() {
        // 查询所有配置
        List<SystemConfig> configs = systemConfigMapper.selectAll();
        
        // 按类别分组
        Map<String, List<SystemConfig>> byCategory = configs.stream()
                .collect(Collectors.groupingBy(SystemConfig::getConfigCategory));

        SystemConfigDTO.RegisterConfig registerConfig = parseRegisterConfig(byCategory.get("register"));
        SystemConfigDTO.SecurityConfig securityConfig = parseSecurityConfig(byCategory.get("security"));
        SystemConfigDTO.TokenConfig tokenConfig = parseTokenConfig(byCategory.get("token"));
        List<SystemConfigDTO.IpWhitelistDTO> ipList = getIpWhitelistList();

        return SystemConfigDTO.builder()
                .register(registerConfig)
                .security(securityConfig)
                .token(tokenConfig)
                .ipWhitelist(ipList)
                .build();
    }

    @Override
    @Transactional
    public void saveAllConfig(SystemConfigDTO configDTO, String operatorId, String operatorName) {
        // 保存注册配置
        saveConfigCategory("register", configDTO.getRegister());
        // 保存安全配置
        saveConfigCategory("security", configDTO.getSecurity());
        // 保存Token配置
        saveConfigCategory("token", configDTO.getToken());
    }

    @Override
    public SystemConfigDTO getIpWhitelist() {
        List<SystemConfigDTO.IpWhitelistDTO> ipList = getIpWhitelistList();
        return SystemConfigDTO.builder()
                .ipWhitelist(ipList)
                .build();
    }

    @Override
    @Transactional
    public void addIpToWhitelist(String ip, String remark, String operatorId) {
        IpWhitelist whitelist = IpWhitelist.builder()
                .ipAddress(ip)
                .remark(remark)
                .createdBy(operatorId)
                .createdAt(LocalDateTime.now())
                .build();
        ipWhitelistMapper.insert(whitelist);
    }

    @Override
    @Transactional
    public void removeIpFromWhitelist(String ip) {
        ipWhitelistMapper.deleteByQuery(QueryWrapper.create()
                .from(IpWhitelist.class)
                .where("ip_address = ?", ip));
    }

    private List<SystemConfigDTO.IpWhitelistDTO> getIpWhitelistList() {
        List<IpWhitelist> whitelist = ipWhitelistMapper.selectAll();
        return whitelist.stream()
                .map(item -> SystemConfigDTO.IpWhitelistDTO.builder()
                        .ip(item.getIpAddress())
                        .remark(item.getRemark())
                        .createTime(item.getCreatedAt() != null ? item.getCreatedAt().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }

    private SystemConfigDTO.RegisterConfig parseRegisterConfig(List<SystemConfig> configs) {
        SystemConfigDTO.RegisterConfig.RegisterConfigBuilder builder = SystemConfigDTO.RegisterConfig.builder();
        
        if (configs == null || configs.isEmpty()) {
            // 返回默认值
            return builder
                    .registerMethods(List.of("邮箱", "手机号"))
                    .forceEmailVerify(true)
                    .needInviteCode(false)
                    .defaultMemberPlan("免费版")
                    .autoActivate(true)
                    .build();
        }

        for (SystemConfig config : configs) {
            try {
                switch (config.getConfigKey()) {
                    case "registerMethods":
                        builder.registerMethods(jacksonUtils.fromJson(config.getConfigValue(), List.class));
                        break;
                    case "forceEmailVerify":
                        builder.forceEmailVerify(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                    case "needInviteCode":
                        builder.needInviteCode(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                    case "defaultMemberPlan":
                        builder.defaultMemberPlan(config.getConfigValue());
                        break;
                    case "autoActivate":
                        builder.autoActivate(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        SystemConfigDTO.RegisterConfig result = builder.build();
        // 确保默认值
        if (result.getRegisterMethods() == null) result.setRegisterMethods(List.of("邮箱", "手机号"));
        if (result.getForceEmailVerify() == null) result.setForceEmailVerify(true);
        if (result.getNeedInviteCode() == null) result.setNeedInviteCode(false);
        if (result.getDefaultMemberPlan() == null) result.setDefaultMemberPlan("免费版");
        if (result.getAutoActivate() == null) result.setAutoActivate(true);
        
        return result;
    }

    private SystemConfigDTO.SecurityConfig parseSecurityConfig(List<SystemConfig> configs) {
        SystemConfigDTO.SecurityConfig.SecurityConfigBuilder builder = SystemConfigDTO.SecurityConfig.builder();
        
        if (configs == null || configs.isEmpty()) {
            return builder
                    .loginMode("混合模式")
                    .enableCaptcha(true)
                    .failLockThreshold(5)
                    .lockMinutes(30)
                    .enable2FA(false)
                    .passwordPolicy(List.of("至少 8 位", "包含数字", "包含特殊字符"))
                    .build();
        }

        for (SystemConfig config : configs) {
            try {
                switch (config.getConfigKey()) {
                    case "loginMode":
                        builder.loginMode(config.getConfigValue());
                        break;
                    case "enableCaptcha":
                        builder.enableCaptcha(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                    case "failLockThreshold":
                        builder.failLockThreshold(Integer.parseInt(config.getConfigValue()));
                        break;
                    case "lockMinutes":
                        builder.lockMinutes(Integer.parseInt(config.getConfigValue()));
                        break;
                    case "enable2FA":
                        builder.enable2FA(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                    case "passwordPolicy":
                        builder.passwordPolicy(jacksonUtils.fromJson(config.getConfigValue(), List.class));
                        break;
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        SystemConfigDTO.SecurityConfig result = builder.build();
        if (result.getLoginMode() == null) result.setLoginMode("混合模式");
        if (result.getEnableCaptcha() == null) result.setEnableCaptcha(true);
        if (result.getFailLockThreshold() == null) result.setFailLockThreshold(5);
        if (result.getLockMinutes() == null) result.setLockMinutes(30);
        if (result.getEnable2FA() == null) result.setEnable2FA(false);
        if (result.getPasswordPolicy() == null) result.setPasswordPolicy(List.of("至少 8 位", "包含数字", "包含特殊字符"));
        
        return result;
    }

    private SystemConfigDTO.TokenConfig parseTokenConfig(List<SystemConfig> configs) {
        SystemConfigDTO.TokenConfig.TokenConfigBuilder builder = SystemConfigDTO.TokenConfig.builder();
        
        if (configs == null || configs.isEmpty()) {
            return builder
                    .accessTokenHours(2)
                    .refreshTokenDays(7)
                    .maxSessionCount(3)
                    .notifyLoginFromNewIp(true)
                    .idleTimeoutMinutes(120)
                    .build();
        }

        for (SystemConfig config : configs) {
            try {
                switch (config.getConfigKey()) {
                    case "accessTokenHours":
                        builder.accessTokenHours(Integer.parseInt(config.getConfigValue()));
                        break;
                    case "refreshTokenDays":
                        builder.refreshTokenDays(Integer.parseInt(config.getConfigValue()));
                        break;
                    case "maxSessionCount":
                        builder.maxSessionCount(Integer.parseInt(config.getConfigValue()));
                        break;
                    case "notifyLoginFromNewIp":
                        builder.notifyLoginFromNewIp(Boolean.parseBoolean(config.getConfigValue()));
                        break;
                    case "idleTimeoutMinutes":
                        builder.idleTimeoutMinutes(Integer.parseInt(config.getConfigValue()));
                        break;
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        SystemConfigDTO.TokenConfig result = builder.build();
        if (result.getAccessTokenHours() == null) result.setAccessTokenHours(2);
        if (result.getRefreshTokenDays() == null) result.setRefreshTokenDays(7);
        if (result.getMaxSessionCount() == null) result.setMaxSessionCount(3);
        if (result.getNotifyLoginFromNewIp() == null) result.setNotifyLoginFromNewIp(true);
        if (result.getIdleTimeoutMinutes() == null) result.setIdleTimeoutMinutes(120);
        
        return result;
    }

    private void saveConfigCategory(String category, Object config) {
        if (config == null) return;
        
        Map<String, Object> configMap;
        try {
            configMap = jacksonUtils.fromJson(jacksonUtils.toJson(config), Map.class);
        } catch (Exception e) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 查询是否已存在
            SystemConfig existing = systemConfigMapper.selectOneByQuery(QueryWrapper.create()
                    .from(SystemConfig.class)
                    .where("config_category = ?", category)
                    .and("config_key = ?", key));

            String jsonValue;
            try {
                jsonValue = value instanceof String ? (String) value : jacksonUtils.toJson(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (existing != null) {
                existing.setConfigValue(jsonValue);
                existing.setUpdatedAt(LocalDateTime.now());
                systemConfigMapper.update(existing);
            } else {
                SystemConfig newConfig = SystemConfig.builder()
                        .configCategory(category)
                        .configKey(key)
                        .configValue(jsonValue)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                systemConfigMapper.insert(newConfig);
            }
        }
    }
}