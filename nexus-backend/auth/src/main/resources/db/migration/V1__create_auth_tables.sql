-- =====================================================
-- CheersAI Nexus 认证服务数据库初始化脚本
-- Version: V1
-- 依赖: 无
-- 说明: 创建认证服务所需的基础表结构
--       - users 表由 user-management 服务初始化，auth 服务依赖此表
--       - audit_logs 表被 auth 和 auditlog 服务共享
--       - refresh_tokens 表存储 JWT 刷新令牌
--       - verification_codes 表存储短信/邮箱验证码
-- =====================================================

-- -----------------------------------------------------
-- 1. 用户表 (users)
-- 说明: 由 user-management 服务创建和维护
--       auth 服务通过此表实现登录功能
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE,
    phone           VARCHAR(20) UNIQUE,
    username        VARCHAR(100) UNIQUE NOT NULL,
    nickname        VARCHAR(100),
    avatar_url      VARCHAR(500),
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'disabled', 'deleted')),
    role            VARCHAR(32) DEFAULT 'user',
    member_plan_code VARCHAR(32) DEFAULT 'free',
    member_expire_at TIMESTAMP,
    email_verified  BOOLEAN DEFAULT false,
    phone_verified  BOOLEAN DEFAULT false,
    last_login_at   TIMESTAMP,
    last_login_ip   VARCHAR(64),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT false,
    version         INTEGER DEFAULT 0,
    CONSTRAINT chk_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_users_email ON nexus.users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON nexus.users(phone);
CREATE INDEX IF NOT EXISTS idx_users_username ON nexus.users(username);
CREATE INDEX IF NOT EXISTS idx_users_status ON nexus.users(status);
CREATE INDEX IF NOT EXISTS idx_users_role ON nexus.users(role);
CREATE INDEX IF NOT EXISTS idx_users_member_plan_code ON nexus.users(member_plan_code);

-- 用户表注释
COMMENT ON TABLE nexus.users IS '用户表 - 存储所有用户基本信息';
COMMENT ON COLUMN nexus.users.id IS '用户唯一标识';
COMMENT ON COLUMN nexus.users.email IS '邮箱（唯一，可为空）';
COMMENT ON COLUMN nexus.users.phone IS '手机号（唯一，可为空）';
COMMENT ON COLUMN nexus.users.username IS '用户名（唯一）';
COMMENT ON COLUMN nexus.users.nickname IS '昵称';
COMMENT ON COLUMN nexus.users.avatar_url IS '头像URL';
COMMENT ON COLUMN nexus.users.password_hash IS '密码哈希（bcrypt）';
COMMENT ON COLUMN nexus.users.status IS '账号状态: active-正常, inactive-未激活, disabled-冻结, deleted-已删除';
COMMENT ON COLUMN nexus.users.role IS '角色: user-普通用户, support-客服, operator-运营, admin-管理员';
COMMENT ON COLUMN nexus.users.member_plan_code IS '会员计划代码: free/pro/team/enterprise';
COMMENT ON COLUMN nexus.users.member_expire_at IS '会员到期时间';
COMMENT ON COLUMN nexus.users.email_verified IS '邮箱是否已验证';
COMMENT ON COLUMN nexus.users.phone_verified IS '手机是否已验证';
COMMENT ON COLUMN nexus.users.last_login_at IS '最后登录时间';
COMMENT ON COLUMN nexus.users.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN nexus.users.deleted IS '逻辑删除标记';
COMMENT ON COLUMN nexus.users.version IS '乐观锁版本号';

-- -----------------------------------------------------
-- 2. 审计日志表 (audit_logs)
-- 说明: 存储系统操作审计日志
--       被 auth 和 auditlog 服务共享
--       日志类型:
--       - user_action: 用户行为（登录、操作记录），保留90天
--       - admin_action: 管理操作（配置变更），保留365天
--       - system_event: 系统事件（启动、错误），保留30天
--       - security_event: 安全事件（登录失败、异常访问），保留365天
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    log_type        VARCHAR(32) NOT NULL CHECK (log_type IN ('user_action', 'admin_action', 'system_event', 'security_event')),
    action          VARCHAR(64) NOT NULL,
    operator_id     VARCHAR(64),
    operator_name   VARCHAR(100),
    target_type     VARCHAR(64),
    target_id       VARCHAR(64),
    before_data     JSONB,
    after_data      JSONB,
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(500),
    result          VARCHAR(16) DEFAULT 'success' CHECK (result IN ('success', 'failure')),
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- 审计日志表索引
CREATE INDEX IF NOT EXISTS idx_audit_logs_log_type ON nexus.audit_logs(log_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON nexus.audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_operator_id ON nexus.audit_logs(operator_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target_type ON nexus.audit_logs(target_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target_id ON nexus.audit_logs(target_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON nexus.audit_logs(ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_logs_result ON nexus.audit_logs(result);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON nexus.audit_logs(created_at DESC);

-- 审计日志表注释
COMMENT ON TABLE nexus.audit_logs IS '审计日志表 - 存储系统操作审计日志';
COMMENT ON COLUMN nexus.audit_logs.log_type IS '日志类型: user_action-用户行为, admin_action-管理操作, system_event-系统事件, security_event-安全事件';
COMMENT ON COLUMN nexus.audit_logs.action IS '操作类型: login/logout/register/create/update/delete等';
COMMENT ON COLUMN nexus.audit_logs.operator_id IS '操作人ID';
COMMENT ON COLUMN nexus.audit_logs.operator_name IS '操作人名称';
COMMENT ON COLUMN nexus.audit_logs.target_type IS '目标对象类型: User/Subscription/Product等';
COMMENT ON COLUMN nexus.audit_logs.target_id IS '目标对象ID';
COMMENT ON COLUMN nexus.audit_logs.before_data IS '操作前数据(JSON)';
COMMENT ON COLUMN nexus.audit_logs.after_data IS '操作后数据(JSON)';
COMMENT ON COLUMN nexus.audit_logs.result IS '操作结果: success/failure';

-- -----------------------------------------------------
-- 3. 刷新令牌表 (refresh_tokens)
-- 说明: 存储 JWT 刷新令牌，支持令牌吊销和过期管理
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES nexus.users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN DEFAULT false,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 刷新令牌表索引
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON nexus.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON nexus.refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON nexus.refresh_tokens(expires_at);

-- 刷新令牌表注释
COMMENT ON TABLE nexus.refresh_tokens IS '刷新令牌表 - 存储JWT刷新令牌';
COMMENT ON COLUMN nexus.refresh_tokens.user_id IS '关联用户ID';
COMMENT ON COLUMN nexus.refresh_tokens.token IS '刷新令牌(唯一)';
COMMENT ON COLUMN nexus.refresh_tokens.expires_at IS '过期时间';
COMMENT ON COLUMN nexus.refresh_tokens.revoked IS '是否已吊销';

-- -----------------------------------------------------
-- 4. 验证码表 (verification_codes)
-- 说明: 存储短信/邮箱验证码，用于注册、登录、密码重置等场景
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS verification_codes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target      VARCHAR(255) NOT NULL,
    code        VARCHAR(10) NOT NULL,
    type        VARCHAR(16) NOT NULL CHECK (type IN ('email', 'phone')),
    purpose     VARCHAR(32) NOT NULL CHECK (purpose IN ('register', 'login', 'reset_password', 'bind', 'change_email', 'change_phone')),
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN DEFAULT false,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 验证码表索引
CREATE INDEX IF NOT EXISTS idx_verification_codes_target ON verification_codes(target);
CREATE INDEX IF NOT EXISTS idx_verification_codes_code ON verification_codes(code);
CREATE INDEX IF NOT EXISTS idx_verification_codes_type_purpose ON verification_codes(type, purpose);
CREATE INDEX IF NOT EXISTS idx_verification_codes_expires_at ON verification_codes(expires_at);

-- 验证码表注释
COMMENT ON TABLE verification_codes IS '验证码表 - 存储短信/邮箱验证码';
COMMENT ON COLUMN verification_codes.target IS '验证码目标(邮箱或手机号)';
COMMENT ON COLUMN verification_codes.code IS '验证码';
COMMENT ON COLUMN verification_codes.type IS '验证码类型: email/phone';
COMMENT ON COLUMN verification_codes.purpose IS '用途: register-注册, login-登录, reset_password-密码重置, bind-绑定, change_email-修改邮箱, change_phone-修改手机';
COMMENT ON COLUMN verification_codes.expires_at IS '过期时间';
COMMENT ON COLUMN verification_codes.used IS '是否已使用';

-- -----------------------------------------------------
-- 5. 初始化默认系统管理员账号
-- 说明: 仅用于首次部署，生产环境应删除或修改密码
--       用户名: admin, 密码: Admin@123456 (bcrypt 加密)
-- -----------------------------------------------------
INSERT INTO nexus.users (id, username, password_hash, email, status, role, email_verified, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiAYvhwgTl.G',  -- Admin@123456
    'admin@cheersai.com',
    'active',
    'admin',
    true,
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;
