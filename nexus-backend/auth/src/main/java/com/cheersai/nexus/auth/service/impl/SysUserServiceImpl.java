package com.cheersai.nexus.auth.service.impl;

import com.cheersai.nexus.auth.mapper.SysUserMapper;
import com.cheersai.nexus.auth.service.SysUserService;
import com.cheersai.nexus.model.base.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @ClassName:SysUserServiceImpl
 * @Description:TODO
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:36
 */
@Service
@Component
public class SysUserServiceImpl implements SysUserService {
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    public SysUser getSysUser(String userId){
        return sysUserMapper.selectOneById(userId);
    }
}
