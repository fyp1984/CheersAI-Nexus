package com.cheersai.nexus.user.service;

import com.cheersai.nexus.user.model.dto.*;
import com.cheersai.nexus.user.model.vo.LoginVO;
import com.cheersai.nexus.user.model.vo.PageResultVO;
import com.cheersai.nexus.user.model.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 密码登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 验证码登录
     */
    LoginVO smsLogin(SmsLoginRequest request);

    /**
     * 发送验证码
     */
    void sendVerifyCode(SendVerifyCodeRequest request);

    /**
     * 刷新Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    void logout(String userId);

    /**
     * 查询用户列表（带分页和筛选）
     */
    PageResultVO<UserVO> queryUsers(UserQueryDTO query);

    /**
     * 获取用户详情
     */
    UserVO getUserById(String userId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(UserStatusUpdateDTO request);

    /**
     * 根据邮箱查询用户
     */
    UserVO getUserByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    UserVO getUserByPhone(String phone);

    /**
     * 根据用户名查询用户
     */
    UserVO getUserByUsername(String username);
}
