package com.cheersai.nexus.user.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.common.utils.JacksonUtils;
import com.cheersai.nexus.user.model.PageUserDTO;
import com.cheersai.nexus.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName:UserManagementController
 * @Description:用户管理功能控制器（Service层开发中）
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:54
 */
@RestController
public class UserManagementController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JacksonUtils jacksonUtils;

    /**
     * 获取所有用户信息
     * @param pageUserDTO 需要查询的传入参数
     * @return Result<String>, 需要反序列化Result.data为Page<User>
     */
    @GetMapping("/api/v1/users")
    public Result<String> getAllUsers(@RequestParam(required = false) PageUserDTO pageUserDTO) {

        if (pageUserDTO == null) {
            pageUserDTO = new PageUserDTO();
            pageUserDTO.setPageNumber(1); // 默认第一页
            pageUserDTO.setPageSize(10);  // 默认每页10条
        }
        
        Page<User> page = userService.getAllUsers(pageUserDTO.getPageNumber(), pageUserDTO.getPageSize(), pageUserDTO.getTotalRow());
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return Result.error("数据未获取成功，请重试");
        }
        try {
            return Result.success(jacksonUtils.toJson(page));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询
     * @param userCondition 查询条件
     * @return 反序列化后为List<User>
     */
    @GetMapping("/api/v1/users/search")
    public Result<String> searchUsers(@RequestParam String userCondition) {
        List<User> userList = userService.searchUsers(userCondition);
        if (userList == null || userList.isEmpty()) {
            return Result.error("数据未获取成功，请重试");
        }
        try {
            return Result.success(jacksonUtils.toJson(userList));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询用户详细信息
     * @param userId 用户ID主键
     * @return 反序列化后为User
     */
    @GetMapping("/api/v1/users/{userId}")
    public Result<String> getUserInfoById(@PathVariable String userId) {
        User user = userService.getUserInfoById(userId);
        if (user == null) {
            return Result.error("数据未获取成功，请重试");
        }
        try {
            return Result.success(jacksonUtils.toJson(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     * @param userId 用户ID主键
     * @param status 用户状态
     * @return 反序列化后为User
     */
    @PutMapping("/api/v1/users/{userId}")
    public Result<String> updateUserStatus(@PathVariable String userId, @RequestParam String status) {
        User user = userService.updateUserStatus(userId, status);
        if (user == null) {
            return Result.error("数据更新失败，请重试");
        }
        try {
            return Result.success(jacksonUtils.toJson(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    
}
