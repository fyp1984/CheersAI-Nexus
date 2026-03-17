package com.cheersai.nexus.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.cheersai.nexus.user.model.RefreshToken;
import com.cheersai.nexus.model.usermanagement.User;
import com.cheersai.nexus.user.model.VerifyCode;
import com.cheersai.nexus.user.enums.UserStatus;
import com.cheersai.nexus.user.mapper.RefreshTokenMapper;
import com.cheersai.nexus.user.mapper.UserMapper;
import com.cheersai.nexus.user.mapper.VerifyCodeMapper;
import com.cheersai.nexus.user.model.dto.*;
import com.cheersai.nexus.user.model.vo.LoginVO;
import com.cheersai.nexus.user.model.vo.PageResultVO;
import com.cheersai.nexus.user.model.vo.UserVO;
import com.cheersai.nexus.user.service.UserService;
import com.cheersai.nexus.user.service.JwtTokenService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ==============================================================
 * 用户服务实现类 - 核心业务逻辑层
 * 职责说明：
 * - 实现UserService接口定义的所有业务方法
 * - 负责用户数据的CRUD操作
 * - 处理业务逻辑、参数校验、事务管理
 * - 协调Mapper层与数据库交互
 * 依赖注入：
 * - UserMapper: 用户数据持久化操作
 * - VerifyCodeMapper: 验证码数据持久化操作
 * - RefreshTokenMapper: 刷新令牌数据持久化操作
 * - JwtTokenService: JWT Token生成与验证
 * - StringRedisTemplate: Redis缓存操作
 * 事务说明：
 * - 所有写操作（注册、状态更新）使用@Transactional注解
 * - 读操作不使用事务以提高性能
 * ==============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // ==================== 依赖注入 ====================
    
    private final UserMapper userMapper;
    private final VerifyCodeMapper verifyCodeMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenService jwtTokenService;
    private final StringRedisTemplate redisTemplate;

    // ==================== 常量定义 ====================
    
    /** Redis验证码缓存前缀 */
    private static final String VERIFY_CODE_CACHE_PREFIX = "verify_code:";
    /** 验证码有效期（分钟） */
    private static final long VERIFY_CODE_EXPIRE_MINUTES = 5;
    /** 验证码发送频率限制（分钟） */
    private static final long VERIFY_CODE_RATE_LIMIT_MINUTES = 1;

    // ==================== 用户注册 ====================
    
    /**
     * ==============================================================
     * 用户注册方法
     * 功能说明：
     * - 处理新用户注册请求
     * - 支持用户名密码注册、邮箱注册、手机号验证码注册
     * - 自动验证邮箱/手机号（如提供验证码）
     * - 使用BCrypt加密存储密码
     * 处理流程：
     * 1. 参数校验（Controller层已完成）
     * 2. 用户名唯一性检查 - 查询是否已存在
     * 3. 邮箱唯一性检查 - 如提供邮箱，验证是否已被注册
     * 4. 手机号唯一性检查 - 如提供手机号，验证是否已被注册
     * 5. 验证码校验 - 如提供验证码，验证是否正确
     * 6. 创建用户实体 - 填充用户信息
     * 7. 密码加密 - 使用BCrypt.hashpw加密
     * 8. 设置初始状态 - 默认为active
     * 9. 验证码注册时自动标记邮箱/手机为已验证
     * 10. 保存到数据库 - 执行插入操作
     * 11. 记录日志 - 记录注册成功信息
     * 异常情况：
     * - 用户名已存在：RuntimeException("用户名已存在")
     * - 邮箱已注册：RuntimeException("邮箱已被注册")
     * - 手机号已注册：RuntimeException("手机号已被注册")
     * - 验证码错误：RuntimeException("验证码错误或已过期")
     * 事务说明：
     * - 整个方法在事务中执行
     * - 任何一步失败都会回滚
     * ==============================================================
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        QueryWrapper nameWrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("username = "+request.getUsername());
        
        if (userMapper.selectCountByQuery(nameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            QueryWrapper emailWrapper = QueryWrapper.create()
                    .select()
                    .from("sys_user")
                    .where("email = "+request.getEmail());
            if (userMapper.selectCountByQuery(emailWrapper) > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            QueryWrapper phoneWrapper = QueryWrapper.create()
                    .select()
                    .from("sys_user")
                    .where("phone = "+request.getPhone());
            if (userMapper.selectCountByQuery(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 如果提供了验证码，验证验证码
        boolean hasVerifyCode = request.getCode() != null && !request.getCode().isEmpty();
        if (hasVerifyCode || (request.getEmail() != null && !request.getEmail().isEmpty()) 
                || (request.getPhone() != null && !request.getPhone().isEmpty())) {
            String target = request.getEmail() != null ? request.getEmail() : request.getPhone();
            validateVerifyCode(target, request.getCode(), "register");
        }

        // 创建用户
        User user = new User();
        user.setUserId(IdUtil.fastSimpleUUID());
        user.setUsername(request.getUsername());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE.getValue());
        
        // 如果使用验证码注册，自动验证邮箱/手机
        if (hasVerifyCode) {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                user.setEmailVerified(true);
            }
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                user.setPhoneVerified(true);
            }
        } else {
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUserId());
    }

    // ==================== 用户登录 ====================
    
    /**
     * ==============================================================
     * 密码登录方法
     * 功能说明：
     * - 使用用户名/邮箱/手机号 + 密码进行身份验证
     * - 验证密码的BCrypt哈希是否匹配
     * - 验证用户状态是否允许登录
     * - 记录最后登录时间和IP
     * - 生成三种JWT Token返回
     * 处理流程：
     * 1. 账号查询 - 根据账号（用户名/邮箱/手机号）查找用户
     * 2. 用户存在性检查 - 用户不存在抛出异常
     * 3. 密码验证 - 使用BCrypt.checkpw验证密码
     * 4. 用户状态检查 - 检查是否active状态
     * 5. 更新登录信息 - 记录最后登录时间和IP
     * 6. 生成Token - 生成Access/Refresh/ID Token
     * 7. 返回结果 - 返回LoginVO对象
     * 异常情况：
     * - 用户不存在：RuntimeException("用户不存在")
     * - 密码错误：RuntimeException("密码错误")
     * - 用户未激活：RuntimeException("账户未激活")
     * - 用户已删除：RuntimeException("账户已被删除")
     * ==============================================================
     */
    @Override
    public LoginVO login(LoginRequest request) {
        User user = findUserByAccount(request.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        checkUserStatus(user);

        // 更新最后登录信息
        updateLastLogin(user, request.getPassword());

        // 生成Token
        return generateLoginVO(user);
    }

    /**
     * ==============================================================
     * 验证码登录方法（手机短信登录）
     * 功能说明：
     * - 使用手机号 + 短信验证码进行快速登录
     * - 验证码校验通过后自动登录
     * - 无需记忆密码，适合移动端场景
     * 处理流程：
     * 1. 验证码校验 - 验证短信验证码是否正确
     * 2. 用户查询 - 根据手机号查找用户
     * 3. 用户存在性检查 - 用户不存在抛出异常
     * 4. 用户状态检查 - 检查是否active状态
     * 5. 更新登录信息 - 记录最后登录时间和IP
     * 6. 生成Token - 生成Access/Refresh/ID Token
     * 7. 返回结果 - 返回LoginVO对象
     * 异常情况：
     * - 验证码错误：RuntimeException("验证码错误或已过期")
     * - 用户不存在：RuntimeException("用户不存在，请先注册")
     * ==============================================================
     */
    @Override
    @Transactional
    public LoginVO smsLogin(SmsLoginRequest request) {
        // 验证验证码
        validateVerifyCode(request.getPhone(), request.getCode(), "login");

        // 查询用户
        QueryWrapper phoneWrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("phone = " + request.getPhone());
        User user = userMapper.selectOneByQuery(phoneWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }

        // 检查用户状态
        checkUserStatus(user);

        // 更新最后登录信息
        updateLastLogin(user, request.getPhone());

        // 生成Token
        return generateLoginVO(user);
    }

    // ==================== 验证码服务 ====================
    
    /**
     * ==============================================================
     * 发送验证码方法
     * 功能说明：
     * - 生成6位数字随机验证码
     * - 存储到Redis（5分钟过期）
     * - 存储到数据库（备查）
     * - 实现发送频率限制（1分钟）
     * 处理流程：
     * 1. 频率检查 - 检查1分钟内是否已发送
     * 2. 生成验证码 - 随机生成6位数字
     * 3. Redis存储 - 设置5分钟过期时间
     * 4. 频率限制 - 设置1分钟冷却时间
     * 5. 实际发送 - TODO: 调用邮件/短信服务
     * 6. 数据库记录 - 存储验证码信息备查
     * 异常情况：
     * - 发送过于频繁：RuntimeException("发送过于频繁，请稍后再试")
     * 注意事项：
     * - 实际发送需要集成邮件服务（如SendGrid、阿里云邮件）
     * - 实际发送需要集成短信服务（如阿里云短信、腾讯云短信）
     * ==============================================================
     */
    @Override
    public void sendVerifyCode(SendVerifyCodeRequest request) {
        String target = request.getTarget();
        String type = request.getType();
        String purpose = request.getPurpose();

        // 检查频率限制
        String rateLimitKey = VERIFY_CODE_CACHE_PREFIX + "rate:" + target;
        if (redisTemplate.hasKey(rateLimitKey)) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        // 生成6位数字验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 存储验证码到Redis
        String cacheKey = VERIFY_CODE_CACHE_PREFIX + type + ":" + target;
        redisTemplate.opsForValue().set(cacheKey, code, VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置频率限制
        redisTemplate.opsForValue().set(rateLimitKey, "1", VERIFY_CODE_RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

        // TODO: 实际发送验证码（邮箱/短信）
        log.info("发送验证码: target={}, type={}, purpose={}, code={}", target, type, purpose, code);

        // 存储验证码到数据库（用于验证）
        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setTarget(target);
        verifyCode.setCode(code);
        verifyCode.setType(type);
        verifyCode.setPurpose(purpose);
        verifyCode.setExpiresAt(LocalDateTime.now().plusMinutes(VERIFY_CODE_EXPIRE_MINUTES));
        verifyCode.setCreatedAt(LocalDateTime.now());
        verifyCodeMapper.insert(verifyCode);
    }

    // ==================== Token刷新 ====================
    
    /**
     * ==============================================================
     * 刷新Token方法
     * 功能说明：
     * - 使用Refresh Token换取新的Access Token
     * - 实现无感知的Token自动续期
     * - 每次刷新都会生成新的Refresh Token（安全考虑）
     * 处理流程：
     * 1. Token验证 - 验证Refresh Token是否有效
     * 2. 用户查询 - 根据Token中的用户ID查找用户
     * 3. 用户存在性检查 - 用户不存在抛出异常
     * 4. 用户状态检查 - 检查是否active状态
     * 5. 撤销旧Token - 将旧Token标记为已撤销
     * 6. 生成新Token - 生成新的三种JWT Token
     * 7. 返回结果 - 返回LoginVO对象
     * 异常情况：
     * - Token无效：RuntimeException("无效的刷新令牌")
     * - 用户不存在：RuntimeException("用户不存在")
     * ==============================================================
     */
    @Override
    @Transactional
    public LoginVO refreshToken(String refreshToken) {
        // 验证Refresh Token
        RefreshToken token = validateRefreshToken(refreshToken);
        if (token == null) {
            throw new RuntimeException("无效的刷新令牌");
        }

        // 查询用户
        User user = userMapper.selectOneById(token.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        checkUserStatus(user);

        // 撤销旧Token
        token.setRevoked(true);
        refreshTokenMapper.update(token);

        // 生成新Token
        return generateLoginVO(user);
    }

    // ==================== 退出登录 ====================
    
    /**
     * ==============================================================
     * 退出登录方法
     * 功能说明：
     * - 清除用户登录状态
     * - 撤销该用户的所有Refresh Token
     * - TODO: 将Access Token加入黑名单
     * 处理流程：
     * 1. 用户ID获取 - 根据用户ID查询
     * 2. Token查询 - 查询所有未撤销的Refresh Token
     * 3. Token撤销 - 逐个标记为已撤销
     * 4. 黑名单处理 - TODO: 将Access Token加入黑名单
     * 5. 记录日志 - 记录退出登录信息
     * ==============================================================
     */
    @Override
    @Transactional
    public void logout(String userId) {
        // 撤销所有Refresh Token
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_refresh_token")
                .where("userid = "+ userId)
                .and("revoked = false ");
        List<RefreshToken> tokens = refreshTokenMapper.selectListByQuery(wrapper);
        if (tokens != null) {
            for (RefreshToken token : tokens) {
                token.setRevoked(true);
                refreshTokenMapper.update(token);
            }
        }

        // TODO: 将Access Token加入黑名单
        log.info("用户退出登录: {}", userId);
    }

    // ==================== 用户查询 ====================
    
    /**
     * ==============================================================
     * 查询用户列表方法
     * 功能说明：
     * - 支持多种条件的分页查询
     * - 关键词搜索（用户名/邮箱/手机号模糊匹配）
     * - 状态筛选（active/inactive/deleted）
     * - 验证状态筛选（邮箱验证、手机验证）
     * 处理流程：
     * 1. 创建查询包装器 - EntityWrapper
     * 2. 构建关键词条件 - 模糊匹配用户名/邮箱/手机号
     * 3. 构建状态条件 - 如提供status参数
     * 4. 构建邮箱验证条件 - 如提供emailVerified参数
     * 5. 构建手机验证条件 - 如提供phoneVerified参数
     * 6. 执行分页查询 - 返回PageResult
     * 7. 数据转换 - 将User列表转换为UserVO列表
     * 8. 构建分页结果 - 组装PageResultVO
     * 性能优化：
     * - 使用索引优化查询（已建索引）
     * - 分页查询避免全表扫描
     * ==============================================================
     */
    @Override
    public PageResultVO<UserVO> queryUsers(UserQueryDTO query) {
        QueryWrapper wrapper = null;

        // 关键词搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
//            wrapper.and(wrapper1 -> wrapper1
//                    .like("username", query.getKeyword())
//                    .or()
//                    .like("email", query.getKeyword())
//                    .or()
//                    .like("phone", query.getKeyword()));
            wrapper = QueryWrapper.create()
                    .select()
                    .from("sys_user")
                    .where("username = "+ query.getKeyword())
                    .or("email like %"+query.getKeyword()+"%")
                    .or("phone like %"+query.getKeyword()+"%");
        }

        // 分页查询
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;

        Page<User> pageResult = null;

        if (wrapper != null) {
            // 状态筛选
            if (query.getStatus() != null) {
                    wrapper.eq("status", query.getStatus().getValue());
            }

            // 邮箱验证筛选
            if (query.getEmailVerified() != null) {
                wrapper.eq("email_verified", query.getEmailVerified());
            }

            // 手机验证筛选
            if (query.getPhoneVerified() != null) {
                wrapper.eq("phone_verified", query.getPhoneVerified());
            }
            pageResult = userMapper.paginate(pageNum, pageSize, wrapper);
        }

        List<UserVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResultVO.of(voList, pageResult.getTotalRow(), pageNum, pageSize);
    }

    /**
     * ==============================================================
     * 根据用户ID获取用户详情
     * 处理流程：
     * 1. 根据ID查询用户实体
     * 2. 用户不存在时抛出异常
     * 3. 转换为VO对象返回
     * ==============================================================
     */
    @Override
    public UserVO getUserById(String userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * ==============================================================
     * 根据邮箱获取用户详情
     * 处理流程：
     * 1. 根据邮箱查询用户
     * 2. 用户不存在时返回null（而非抛出异常）
     * 3. 转换为VO对象返回
     * ==============================================================
     */
    @Override
    public UserVO getUserByEmail(String email) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("email = "+ email);
        User user = userMapper.selectOneByQuery(wrapper);
        return user != null ? convertToVO(user) : null;
    }

    /**
     * ==============================================================
     * 根据手机号获取用户详情
     * 处理流程：
     * 1. 根据手机号查询用户
     * 2. 用户不存在时返回null
     * 3. 转换为VO对象返回
     * ==============================================================
     */
    @Override
    public UserVO getUserByPhone(String phone) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("phone = "+ phone);
        User user = userMapper.selectOneByQuery(wrapper);
        return user != null ? convertToVO(user) : null;
    }

    /**
     * ==============================================================
     * 根据用户名获取用户详情
     * 处理流程：
     * 1. 根据用户名查询用户
     * 2. 用户不存在时返回null
     * 3. 转换为VO对象返回
     * ==============================================================
     */
    @Override
    public UserVO getUserByUsername(String username) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("username = "+ username);
        User user = userMapper.selectOneByQuery(wrapper);
        return user != null ? convertToVO(user) : null;
    }

    // ==================== 用户状态管理 ====================
    
    /**
     * ==============================================================
     * 更新用户状态方法
     * 功能说明：
     * - 修改用户账号状态
     * - 支持active/inactive/deleted三种状态
     * - 更新updated_at时间戳
     * 处理流程：
     * 1. 根据ID查询用户
     * 2. 用户不存在时抛出异常
     * 3. 更新状态字段
     * 4. 更新时间戳
     * 5. 持久化到数据库
     * 6. 记录日志
     * ==============================================================
     */
    @Override
    @Transactional
    public void updateUserStatus(UserStatusUpdateDTO request) {
        User user = userMapper.selectOneById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(request.getStatus().getValue());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        log.info("更新用户状态: userId={}, status={}", request.getUserId(), request.getStatus());
    }

    // ==================== 私有辅助方法 ====================
    
    /**
     * ==============================================================
     * 根据账号查找用户
     * 功能：支持用户名、邮箱、手机号三种方式查询
     * 参数：account - 用户名/邮箱/手机号
     * 返回：User实体或null
     * ==============================================================
     */
    private User findUserByAccount(String account) {

        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("email = "+ account)
                .or("phone = "+ account)
                .or("username = "+ account);

        return userMapper.selectOneByQuery(wrapper);
    }

    /**
     * ==============================================================
     * 检查用户状态
     * 功能：验证用户是否允许登录
     * 参数：user - 用户实体
     * 异常：
     * - 账户未激活：RuntimeException("账户未激活")
     * - 账户已删除：RuntimeException("账户已被删除")
     * ==============================================================
     */
    private void checkUserStatus(User user) {
        String status = user.getStatus();
        if (UserStatus.INACTIVE.getValue().equals(status)) {
            throw new RuntimeException("账户未激活");
        }
        if (UserStatus.DELETED.getValue().equals(status)) {
            throw new RuntimeException("账户已被删除");
        }
    }

    /**
     * ==============================================================
     * 验证验证码
     * 功能：校验验证码是否正确且未过期
     * 参数：
     * - target: 邮箱/手机号
     * - code: 用户输入的验证码
     * - purpose: 用途（register/login/reset_password）
     * 异常：验证码错误或已过期
     * ==============================================================
     */
    private void validateVerifyCode(String target, String code, String purpose) {
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }

        String cacheKey = VERIFY_CODE_CACHE_PREFIX + 
                (target.contains("@") ? "email" : "phone") + ":" + target;
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);

        if (cachedCode == null) {
            // 检查数据库中的验证码
            QueryWrapper wrapper = QueryWrapper.create()
                    .select()
                    .from("sys_verify_code")
                    .where("target = "+ target)
                    .and("purpose = "+ purpose)
                    .ge("expires_at", LocalDateTime.now())
                    .orderBy("expires_at desc");
            VerifyCode verifyCode = verifyCodeMapper.selectOneByQuery(wrapper);
            
            if (verifyCode == null || !verifyCode.getCode().equals(code)) {
                throw new RuntimeException("验证码错误或已过期");
            }
            
            // 验证成功后删除验证码
            verifyCodeMapper.deleteById(verifyCode.getId());
        } else {
            if (!cachedCode.equals(code)) {
                throw new RuntimeException("验证码错误");
            }
            // 验证成功后删除验证码
            redisTemplate.delete(cacheKey);
        }
    }

    /**
     * ==============================================================
     * 更新最后登录信息
     * 功能：记录用户最后登录时间和IP
     * 参数：
     * - user: 用户实体
     * - loginIp: 登录IP
     * ==============================================================
     */
    @Transactional
    protected void updateLastLogin(User user, String loginIp) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * ==============================================================
     * 生成登录响应对象
     * 功能：生成完整的登录响应，包含三种Token和用户信息
     * 参数：user - 已验证的用户实体
     * 返回：LoginVO对象
     * Token说明：
     * - Access Token: 短期访问令牌，有效期1小时
     * - Refresh Token: 刷新令牌，有效期7天
     * - ID Token: 身份令牌，有效期30天
     * ==============================================================
     */
    private LoginVO generateLoginVO(User user) {
        LoginVO loginVO = new LoginVO();

        // 生成Access Token
        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getUsername());
        loginVO.setAccessToken(accessToken);
        loginVO.setAccessTokenExpireIn(jwtTokenService.getAccessTokenExpireIn());

        // 生成Refresh Token
        String refreshToken = jwtTokenService.generateRefreshToken(user.getUserId());
        loginVO.setRefreshToken(refreshToken);
        loginVO.setRefreshTokenExpireIn(jwtTokenService.getRefreshTokenExpireIn());

        // 生成ID Token
        String idToken = jwtTokenService.generateIdToken(user);
        loginVO.setIdToken(idToken);
        loginVO.setIdTokenExpireIn(jwtTokenService.getIdTokenExpireIn());

        // 存储Refresh Token
        saveRefreshToken(user.getUserId(), refreshToken);

        // 设置用户信息
        loginVO.setUser(convertToVO(user));

        return loginVO;
    }

    /**
     * ==============================================================
     * 保存Refresh Token到数据库
     * 功能：将生成的Refresh Token持久化存储
     * 参数：
     * - userId: 用户ID
     * - token: Refresh Token字符串
     * ==============================================================
     */
    private void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshTokenExpireIn() / 1000));
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setRevoked(false);
        refreshTokenMapper.insert(refreshToken);
    }

    /**
     * ==============================================================
     * 验证Refresh Token
     * 功能：验证Token是否有效且未过期
     * 参数：token - Refresh Token字符串
     * 返回：RefreshToken实体或null
     * ==============================================================
     */
    private RefreshToken validateRefreshToken(String token) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from("sys_user")
                .where("token = "+ token)
                .and("revoked = false ")
                .and("expires_at = now() ");
        return refreshTokenMapper.selectOneByQuery(wrapper);
    }

    /**
     * ==============================================================
     * User实体转换为UserVO
     * 功能：将数据实体转换为视图对象
     * 参数：user - User实体
     * 返回：UserVO视图对象
     * 转换说明：
     * - 敏感信息（passwordHash）不包含在VO中
     * - 时间格式转换（LocalDateTime）
     * ==============================================================
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setEmailVerified(user.getEmailVerified());
        vo.setPhoneVerified(user.getPhoneVerified());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
