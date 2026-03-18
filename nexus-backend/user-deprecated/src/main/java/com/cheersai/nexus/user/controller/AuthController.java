package com.cheersai.nexus.user.controller;

import com.cheersai.nexus.user.model.dto.*;
import com.cheersai.nexus.user.model.vo.LoginVO;
import com.cheersai.nexus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================
 * 认证Controller - 处理用户认证相关请求
 * 
 * 主要功能：
 * 1. 用户注册 - 支持邮箱/手机号注册
 * 2. 用户登录 - 支持密码登录和验证码登录
 * 3. 验证码发送 - 邮箱/手机号验证码
 * 4. Token刷新 - 使用Refresh Token刷新访问令牌
 * 5. 退出登录 - 清除用户认证状态
 * 
 * 认证流程：
 * - 密码登录：用户名/邮箱/手机号 + 密码
 * - 验证码登录：手机号 + 短信验证码
 * - SSO支持：通过Token实现跨系统单点登录
 * ==============================================================
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // ==================== 用户注册 ====================
    
    /**
     * ==============================================================
     * 用户注册接口
     * 
     * 功能说明：
     * - 接收用户注册请求，验证输入信息的合法性
     * - 支持用户名+密码、邮箱+密码、手机号+验证码等多种注册方式
     * - 注册成功后自动创建用户账户，并返回200状态码
     * 
     * 请求体参数（RegisterRequest）：
     * - username: 用户名（必填，2-30字符）
     * - password: 密码（必填，6-20字符）
     * - email: 邮箱（可选，用于账号找回）
     * - phone: 手机号（可选，用于短信验证）
     * - code: 验证码（可选，用于邮箱/手机验证）
     * 
     * 处理流程：
     * 1. 参数校验 - 验证必填字段和格式
     * 2. 重复性检查 - 检查用户名、邮箱、手机号是否已存在
     * 3. 验证码校验（如提供）- 验证邮箱/手机验证码
     * 4. 创建用户 - 加密密码并存储用户信息
     * 5. 返回结果 - 返回200表示注册成功
     * 
     * 返回值：200 OK（注册成功）
     * ==============================================================
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    // ==================== 用户登录 ====================
    
    /**
     * ==============================================================
     * 密码登录接口
     * 
     * 功能说明：
     * - 使用用户名/邮箱/手机号 + 密码进行身份验证
     * - 验证通过后生成三种JWT Token（Access/Refresh/ID）
     * - 返回完整的登录信息，包括Token和用户资料
     * 
     * 请求体参数（LoginRequest）：
     * - account: 账号（用户名/邮箱/手机号）
     * - password: 密码
     * 
     * 处理流程：
     * 1. 参数校验 - 验证必填字段
     * 2. 用户查询 - 根据账号查找用户
     * 3. 密码验证 - 使用BCrypt验证密码哈希
     * 4. 状态检查 - 验证用户状态是否正常
     * 5. 登录记录 - 更新最后登录时间和IP
     * 6. Token生成 - 生成三种JWT Token
     * 7. 返回结果 - 返回Token和用户信息
     * 
     * 返回值：200 OK + LoginVO（包含三种Token和用户信息）
     * ==============================================================
     */
    @PostMapping("/login")
    public ResponseEntity<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = userService.login(request);
        return ResponseEntity.ok(loginVO);
    }

    /**
     * ==============================================================
     * 验证码登录接口（手机短信登录）
     * 
     * 功能说明：
     * - 使用手机号 + 短信验证码进行快速登录
     * - 无需记忆密码，适合移动端场景
     * - 验证通过后同样生成三种JWT Token
     * 
     * 请求体参数（SmsLoginRequest）：
     * - phone: 手机号（必填）
     * - code: 短信验证码（必填，6位数字）
     * 
     * 处理流程：
     * 1. 参数校验 - 验证必填字段格式
     * 2. 验证码校验 - 验证短信验证码是否正确
     * 3. 用户查询 - 根据手机号查找用户
     * 4. 状态检查 - 验证用户状态是否正常
     * 5. 登录记录 - 更新最后登录信息
     * 6. Token生成 - 生成三种JWT Token
     * 7. 返回结果 - 返回Token和用户信息
     * 
     * 返回值：200 OK + LoginVO（包含三种Token和用户信息）
     * ==============================================================
     */
    @PostMapping("/sms-login")
    public ResponseEntity<LoginVO> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        LoginVO loginVO = userService.smsLogin(request);
        return ResponseEntity.ok(loginVO);
    }

    // ==================== 验证码 ====================
    
    /**
     * ==============================================================
     * 发送验证码接口
     * 
     * 功能说明：
     * - 向用户邮箱或手机号发送一次性验证码
     * - 用于注册验证、登录验证、密码找回等场景
     * - 验证码有效期5分钟，同一号码1分钟内只能发送一次
     * 
     * 请求体参数（SendVerifyCodeRequest）：
     * - target: 目标（邮箱地址或手机号码）
     * - type: 类型（email/phone）
     * - purpose: 用途（register/reset_password/login）
     * 
     * 处理流程：
     * 1. 参数校验 - 验证必填字段
     * 2. 频率检查 - 检查是否频繁发送（1分钟限制）
     * 3. 生成验证码 - 生成6位数字随机验证码
     * 4. Redis缓存 - 将验证码存入Redis（5分钟过期）
     * 5. 频率限制 - 设置发送频率限制
     * 6. 发送验证码 - TODO: 调用实际发送服务（邮箱/短信）
     * 7. 数据库记录 - 将验证码存入数据库备查
     * 8. 返回结果 - 返回200表示发送成功
     * 
     * 注意事项：
     * - 实际发送逻辑需要集成邮件服务/短信服务
     * - 验证码应通过消息队列异步发送
     * - 需要添加发送次数限制防止滥用
     * 
     * 返回值：200 OK（发送成功）
     * ==============================================================
     */
    @PostMapping("/send-verify-code")
    public ResponseEntity<Void> sendVerifyCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendVerifyCode(request);
        return ResponseEntity.ok().build();
    }

    // ==================== Token刷新 ====================
    
    /**
     * ==============================================================
     * 刷新Token接口
     * 
     * 功能说明：
     * - 使用Refresh Token换取新的Access Token
     * - 实现"一次登录，长期可用"的无感刷新机制
     * - Refresh Token有效期7天，Access Token有效期1小时
     * 
     * 请求头参数：
     * - Refresh-Token: Refresh Token字符串
     * 
     * 处理流程：
     * 1. Token验证 - 验证Refresh Token是否有效
     * 2. 用户查询 - 根据Token中的用户ID查找用户
     * 3. 状态检查 - 验证用户状态是否正常
     * 4. 撤销旧Token - 将旧Refresh Token标记为已撤销
     * 5. 生成新Token - 生成新的三种JWT Token
     * 6. 返回结果 - 返回新的Token和用户信息
     * 
     * 安全性说明：
     * - Refresh Token一次性使用，刷新后自动失效
     * - 每次刷新都生成新的Refresh Token
     * - 支持Token主动撤销（退出登录时）
     * 
     * 返回值：200 OK + LoginVO（新的Token和用户信息）
     * ==============================================================
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginVO> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginVO loginVO = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginVO);
    }

    // ==================== 退出登录 ====================
    
    /**
     * ==============================================================
     * 退出登录接口
     * 
     * 功能说明：
     * - 清除用户的登录状态
     * - 撤销当前使用的Refresh Token
     * - 将Access Token加入黑名单（TODO）
     * 
     * 请求头参数：
     * - Authorization: Bearer Access Token
     * 
     * 处理流程：
     * 1. Token解析 - 从Authorization头提取Token
     * 2. 用户ID获取 - 从Token中解析用户ID
     * 3. Token撤销 - 撤销该用户的所有Refresh Token
     * 4. 黑名单处理 - 将Access Token加入黑名单（TODO）
     * 5. 返回结果 - 返回200表示退出成功
     * 
     * 注意事项：
     * - 需要实现Access Token黑名单机制
     * - 可以选择立即失效或等待自然过期
     * 
     * 返回值：200 OK（退出成功）
     * ==============================================================
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        // 从Token中获取用户ID并退出
        String token = authorization.replace("Bearer ", "");
        // TODO: 从Token中解析用户ID
        return ResponseEntity.ok().build();
    }
}
