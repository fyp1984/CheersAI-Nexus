package com.cheersai.nexus.systemconfig.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.systemconfig.dto.SystemConfigDTO;
import com.cheersai.nexus.systemconfig.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping
    public Result<SystemConfigDTO> getAllConfig() {
        return Result.success(systemConfigService.getAllConfig());
    }

    /**
     * 保存所有系统配置
     */
    @PutMapping
    public Result<Void> saveAllConfig(@RequestBody SystemConfigDTO configDTO,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String operatorId = userId != null ? userId : "admin";
        String operatorName = userName != null ? userName : "管理员";
        
        systemConfigService.saveAllConfig(configDTO, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 获取IP白名单
     */
    @GetMapping("/ip-whitelist")
    public Result<SystemConfigDTO> getIpWhitelist() {
        return Result.success(systemConfigService.getIpWhitelist());
    }

    /**
     * 添加IP到白名单
     */
    @PostMapping("/ip-whitelist")
    public Result<Void> addIpToWhitelist(@RequestBody Map<String, String> request,
                                       @RequestHeader(value = "X-User-Id", required = false) String userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String ip = request.get("ip");
        String remark = request.get("remark");
        String operatorId = userId != null ? userId : "admin";
        
        systemConfigService.addIpToWhitelist(ip, remark, operatorId);
        return Result.success();
    }

    /**
     * 从白名单移除IP
     */
    @DeleteMapping("/ip-whitelist/{ip}")
    public Result<Void> removeIpFromWhitelist(@PathVariable("ip") String ip) {
        systemConfigService.removeIpFromWhitelist(ip);
        return Result.success();
    }
}