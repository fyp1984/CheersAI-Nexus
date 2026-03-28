package com.cheersai.nexus.systemconfig.service;

import com.cheersai.nexus.systemconfig.dto.SystemConfigDTO;

/**
 * 系统配置业务逻辑接口
 */
public interface SystemConfigService {

    /**
     * 获取所有系统配置
     */
    SystemConfigDTO getAllConfig();

    /**
     * 保存所有系统配置
     */
    void saveAllConfig(SystemConfigDTO configDTO, String operatorId, String operatorName);

    /**
     * 获取IP白名单
     */
    SystemConfigDTO getIpWhitelist();

    /**
     * 添加IP到白名单
     */
    void addIpToWhitelist(String ip, String remark, String operatorId);

    /**
     * 从白名单移除IP
     */
    void removeIpFromWhitelist(String ip);
}