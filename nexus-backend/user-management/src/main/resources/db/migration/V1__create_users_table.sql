-- =====================================================
-- CheersAI Nexus 用户管理服务数据库初始化脚本
-- Version: V1
-- 依赖: 无
-- 说明: 创建用户管理服务所需的基础表结构
--       - users 表是核心表，被 auth 服务共享使用
--       此脚本与 auth 服务的迁移脚本互补，确保 users 表存在
-- =====================================================

-- -----------------------------------------------------
-- 1. 用户表 (users)
-- 说明: 存储所有用户基本信息
--       被 auth 服务共享使用（登录功能）
--       被 membership 服务使用（订阅管理）
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
CREATE INDEX IF NOT EXISTS idx_users_created_at ON nexus.users(created_at DESC);

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
-- 2. 用户操作日志表 (user_operation_logs)
-- 说明: 记录用户相关的管理操作日志
--       与 audit_logs 表互补，记录更详细的用户管理操作
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.user_operation_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    action          VARCHAR(50) NOT NULL,
    target_type     VARCHAR(50) NOT NULL,
    target_id       VARCHAR(64),
    before_data     JSONB,
    after_data      JSONB,
    operator_id     VARCHAR(64),
    operator_name   VARCHAR(100),
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(500),
    result          VARCHAR(16) DEFAULT 'success',
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- 用户操作日志表索引
CREATE INDEX IF NOT EXISTS idx_user_operation_logs_user_id ON nexus.user_operation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_operation_logs_action ON nexus.user_operation_logs(action);
CREATE INDEX IF NOT EXISTS idx_user_operation_logs_target_type ON nexus.user_operation_logs(target_type);
CREATE INDEX IF NOT EXISTS idx_user_operation_logs_operator_id ON nexus.user_operation_logs(operator_id);
CREATE INDEX IF NOT EXISTS idx_user_operation_logs_created_at ON nexus.user_operation_logs(created_at DESC);

-- 用户操作日志表注释
COMMENT ON TABLE nexus.user_operation_logs IS '用户操作日志表 - 记录用户相关的管理操作';
COMMENT ON COLUMN nexus.user_operation_logs.user_id IS '关联用户ID';
COMMENT ON COLUMN nexus.user_operation_logs.action IS '操作类型: create/update/delete/status/role/member';
COMMENT ON COLUMN nexus.user_operation_logs.target_type IS '目标类型: user/profile/subscription';
