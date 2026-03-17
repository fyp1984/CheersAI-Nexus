package com.cheersai.nexus.user.service.impl;

import com.cheersai.nexus.model.usermanagement.User;
import com.cheersai.nexus.user.mapper.UserMapper;
import com.cheersai.nexus.user.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @ClassName:UserServiceImpl
 * @Description:TODO
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:57
 */

@Service
@Component
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public Page<User> getAllUsers(int pageNumber, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("user");
        return userMapper.paginate(pageNumber, pageSize, wrapper);
    }

    public Page<User> getAllUsers(int pageNumber, int pageSize, int totalRow) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("user");
        return userMapper.paginate(pageNumber, pageSize, totalRow, wrapper);
    }

}
