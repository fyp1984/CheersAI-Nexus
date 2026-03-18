package com.cheersai.nexus.user.controller;

import com.cheersai.nexus.user.model.dto.UserQueryDTO;
import com.cheersai.nexus.user.model.dto.UserStatusUpdateDTO;
import com.cheersai.nexus.user.model.vo.PageResultVO;
import com.cheersai.nexus.user.model.vo.UserVO;
import com.cheersai.nexus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================
 * 用户管理Controller - 处理用户信息管理相关请求
 * 
 * 主要功能：
 * 1. 用户列表查询 - 支持分页、关键词搜索、状态筛选
 * 2. 用户详情查看 - 根据ID/邮箱/手机号/用户名查询
 * 3. 用户状态管理 - 激活、禁用、删除用户
 * 
 * 权限说明：
 * - 所有接口需要有效的Access Token认证
 * - 建议添加角色权限控制（如管理员）
 * 
 * 分页说明：
 * - 默认每页10条记录
 * - 支持自定义页码和每页数量
 * ==============================================================
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 用户列表查询 ====================
    
    /**
     * ==============================================================
     * 查询用户列表接口（带分页和筛选）
     * 
     * 功能说明：
     * - 获取用户列表，支持分页展示
     * - 支持关键词搜索（用户名/邮箱/手机号模糊匹配）
     * - 支持多种筛选条件（状态、邮箱验证、手机验证）
     * - 返回分页结果，包含数据列表和总数
     * 
     * 查询参数（UserQueryDTO）：
     * - keyword: 关键词搜索（可选，用户名/邮箱/手机号模糊匹配）
     * - status: 用户状态筛选（可选，active/inactive/deleted）
     * - emailVerified: 邮箱验证状态筛选（可选，true/false）
     * - phoneVerified: 手机验证状态筛选（可选，true/false）
     * - pageNum: 页码（可选，默认1）
     * - pageSize: 每页数量（可选，默认10，最大100）
     * 
     * 处理流程：
     * 1. 参数接收 - 接收查询条件和分页参数
     * 2. 条件构建 - 根据条件构建MyBatis-Flex查询包装器
     * 3. 分页查询 - 执行分页查询获取用户列表
     * 4. 数据转换 - 将User实体转换为UserVO视图对象
     * 5. 返回结果 - 返回分页结果（列表+总数+分页信息）
     * 
     * 返回值：200 OK + PageResultVO<UserVO>
     * - list: 用户列表
     * - total: 总记录数
     * - pageNum: 当前页码
     * - pageSize: 每页数量
     * - totalPages: 总页数
     * ==============================================================
     */
    @GetMapping
    public ResponseEntity<PageResultVO<UserVO>> queryUsers(@ModelAttribute UserQueryDTO query) {
        PageResultVO<UserVO> result = userService.queryUsers(query);
        return ResponseEntity.ok(result);
    }

    // ==================== 用户详情查询 ====================
    
    /**
     * ==============================================================
     * 获取用户详情接口（根据用户ID）
     * 
     * 功能说明：
     * - 根据用户唯一标识（UUID）获取用户详细信息
     * - 返回完整的用户资料，包括认证状态和登录信息
     * 
     * 路径参数：
     * - userId: 用户唯一标识（UUID格式）
     * 
     * 处理流程：
     * 1. 参数接收 - 接收用户ID
     * 2. 用户查询 - 根据ID查询用户实体
     * 3. 结果判断 - 用户不存在时返回404
     * 4. 数据转换 - 将User实体转换为UserVO
     * 5. 返回结果 - 返回用户详情
     * 
     * 返回值：
     * - 200 OK + UserVO（用户详情）
     * - 404 Not Found（用户不存在）
     * ==============================================================
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserVO> getUserById(@PathVariable String userId) {
        UserVO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * ==============================================================
     * 获取用户详情接口（根据邮箱）
     * 
     * 功能说明：
     * - 根据用户邮箱地址查询用户信息
     * - 用于邮箱验证、账号找回等场景
     * 
     * 查询参数：
     * - email: 邮箱地址
     * 
     * 处理流程：
     * 1. 参数接收 - 接收邮箱地址
     * 2. 用户查询 - 根据邮箱查询用户
     * 3. 结果判断 - 用户不存在时返回404
     * 4. 数据转换 - 将User实体转换为UserVO
     * 5. 返回结果 - 返回用户详情
     * 
     * 返回值：
     * - 200 OK + UserVO（用户详情）
     * - 404 Not Found（用户不存在）
     * ==============================================================
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserVO> getUserByEmail(@RequestParam String email) {
        UserVO user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * ==============================================================
     * 获取用户详情接口（根据手机号）
     * 
     * 功能说明：
     * - 根据用户手机号码查询用户信息
     * - 用于短信登录、号码绑定等场景
     * 
     * 查询参数：
     * - phone: 手机号码
     * 
     * 处理流程：
     * 1. 参数接收 - 接收手机号码
     * 2. 用户查询 - 根据手机号查询用户
     * 3. 结果判断 - 用户不存在时返回404
     * 4. 数据转换 - 将User实体转换为UserVO
     * 5. 返回结果 - 返回用户详情
     * 
     * 返回值：
     * - 200 OK + UserVO（用户详情）
     * - 404 Not Found（用户不存在）
     * ==============================================================
     */
    @GetMapping("/by-phone")
    public ResponseEntity<UserVO> getUserByPhone(@RequestParam String phone) {
        UserVO user = userService.getUserByPhone(phone);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * ==============================================================
     * 获取用户详情接口（根据用户名）
     * 
     * 功能说明：
     * - 根据用户名查询用户信息
     * - 用于用户主页、用户搜索等场景
     * 
     * 查询参数：
     * - username: 用户名
     * 
     * 处理流程：
     * 1. 参数接收 - 接收用户名
     * 2. 用户查询 - 根据用户名查询用户
     * 3. 结果判断 - 用户不存在时返回404
     * 4. 数据转换 - 将User实体转换为UserVO
     * 5. 返回结果 - 返回用户详情
     * 
     * 返回值：
     * - 200 OK + UserVO（用户详情）
     * - 404 Not Found（用户不存在）
     * ==============================================================
     */
    @GetMapping("/by-username")
    public ResponseEntity<UserVO> getUserByUsername(@RequestParam String username) {
        UserVO user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // ==================== 用户状态管理 ====================
    
    /**
     * ==============================================================
     * 更新用户状态接口
     * 
     * 功能说明：
     * - 修改用户账号的状态（激活/禁用/删除）
     * - 支持三种状态切换：
     *   - active: 正常状态，用户可以正常登录
     *   - inactive: 未激活/禁用状态，禁止登录
     *   - deleted: 已删除状态，永久禁用
     * 
     * 请求体参数（UserStatusUpdateDTO）：
     * - userId: 用户ID（必填）
     * - status: 目标状态（必填，active/inactive/deleted）
     * 
     * 处理流程：
     * 1. 参数校验 - 验证必填字段和状态值合法性
     * 2. 用户查询 - 根据ID查询用户是否存在
     * 3. 状态更新 - 修改用户状态字段
     * 4. 更新时间 - 更新updated_at时间戳
     * 5. 数据库保存 - 持久化到数据库
     * 6. 返回结果 - 返回200表示更新成功
     * 
     * 注意事项：
     * - 删除操作应为逻辑删除，不真正删除数据
     * - 状态变更应记录操作日志
     * - 删除用户应同时撤销其所有Token
     * 
     * 返回值：200 OK（更新成功）
     * ==============================================================
     */
    @PutMapping("/status")
    public ResponseEntity<Void> updateUserStatus(@Valid @RequestBody UserStatusUpdateDTO request) {
        userService.updateUserStatus(request);
        return ResponseEntity.ok().build();
    }
}
