package com.cheersai.nexus.auth.controller;

import com.cheersai.nexus.auth.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName:AuthController
 * @Description:系统用户注册及登录接口（暂未开发）
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:21
 */
@RestController("/auth")
public class AuthController {
    
    @Autowired
    private SysUserService sysUserService;
    
    
}
