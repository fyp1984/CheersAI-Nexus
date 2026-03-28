-- =====================================================
-- CheersAI Nexus 用户管理服务数据库优化脚本
-- Version: V2
-- 依赖: V1__create_users_table.sql
-- 说明: 创建额外的用户管理相关表和优化索引
-- =====================================================

-- -----------------------------------------------------
-- 1. 用户会话表 (user_sessions)
-- 说明: 记录用户登录会话信息，支持多设备登录控制
--       与 auth 服务的 refresh_tokens 互补
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.user_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES nexus.users(id) ON DELETE CASCADE,
    session_id      VARCHAR(128) NOT NULL UNIQUE,
    device_type     VARCHAR(32),
    device_name     VARCHAR(100),
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(500),
    login_at        TIMESTAMP DEFAULT NOW(),
    last_active_at  TIMESTAMP DEFAULT NOW(),
    expires_at      TIMESTAMP,
    revoked         BOOLEAN DEFAULT false,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- 用户会话表索引
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON nexus.user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_session_id ON nexus.user_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_login_at ON nexus.user_sessions(login_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_sessions_last_active_at ON nexus.user_sessions(last_active_at DESC);

-- 用户会话表注释
COMMENT ON TABLE nexus.user_sessions IS '用户会话表 - 记录用户登录会话';
COMMENT ON COLUMN nexus.user_sessions.user_id IS '关联用户ID';
COMMENT ON COLUMN nexus.user_sessions.session_id IS '会话ID(唯一)';
COMMENT ON COLUMN nexus.user_sessions.device_type IS '设备类型: web/ios/android/desktop';
COMMENT ON COLUMN nexus.user_sessions.device_name IS '设备名称';
COMMENT ON COLUMN nexus.user_sessions.ip_address IS '登录IP';
COMMENT ON COLUMN nexus.user_sessions.user_agent IS '浏览器/客户端信息';
COMMENT ON COLUMN nexus.user_sessions.login_at IS '登录时间';
COMMENT ON COLUMN nexus.user_sessions.last_active_at IS '最后活跃时间';
COMMENT ON COLUMN nexus.user_sessions.expires_at IS '会话过期时间';
COMMENT ON COLUMN nexus.user_sessions.revoked IS '是否已吊销';

-- -----------------------------------------------------
-- 2. 用户行为追踪表 (user_behavior_tracking)
-- 说明: 记录用户行为数据，用于分析和风控
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.user_behavior_tracking (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES nexus.users(id) ON DELETE SET NULL,
    event_type      VARCHAR(64) NOT NULL,
    event_data      JSONB DEFAULT '{}',
    ip_address      VARCHAR(64),
    location        VARCHAR(200),
    device_fingerprint VARCHAR(128),
    session_id      VARCHAR(128),
    created_at      TIMESTAMP DEFAULT NOW()
);

-- 用户行为追踪表索引
CREATE INDEX IF NOT EXISTS idx_user_behavior_tracking_user_id ON nexus.user_behavior_tracking(user_id);
CREATE INDEX IF NOT EXISTS idx_user_behavior_tracking_event_type ON nexus.user_behavior_tracking(event_type);
CREATE INDEX IF NOT EXISTS idx_user_behavior_tracking_created_at ON nexus.user_behavior_tracking(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_behavior_tracking_ip_address ON nexus.user_behavior_tracking(ip_address);

-- 用户行为追踪表注释
COMMENT ON TABLE nexus.user_behavior_tracking IS '用户行为追踪表 - 记录用户行为数据';
COMMENT ON COLUMN nexus.user_behavior_tracking.user_id IS '关联用户ID(可为空，用于未登录用户)';
COMMENT ON COLUMN nexus.user_behavior_tracking.event_type IS '事件类型';
COMMENT ON COLUMN nexus.user_behavior_tracking.event_data IS '事件数据(JSON)';
COMMENT ON COLUMN nexus.user_behavior_tracking.location IS '地理位置';
COMMENT ON COLUMN nexus.user_behavior_tracking.device_fingerprint IS '设备指纹';

-- -----------------------------------------------------
-- 3. 额外的用户表复合索引优化
-- 说明: 为常见查询模式添加复合索引
-- -----------------------------------------------------

-- 复合索引: 按状态和角色查询用户
CREATE INDEX IF NOT EXISTS idx_users_status_role ON nexus.users(status, role);

-- 复合索引: 按会员计划和状态查询用户
CREATE INDEX IF NOT EXISTS idx_users_member_plan_status ON nexus.users(member_plan_code, status);

-- 复合索引: 按创建时间和状态查询用户（用于数据分析）
CREATE INDEX IF NOT EXISTS idx_users_created_status ON nexus.users(created_at DESC, status);

-- 复合索引: 查找未验证邮箱的用户
CREATE INDEX IF NOT EXISTS idx_users_email_unverified ON nexus.users(email) WHERE email_verified = false;

-- 复合索引: 查找未验证手机的用户
CREATE INDEX IF NOT EXISTS idx_users_phone_unverified ON nexus.users(phone) WHERE phone_verified = false;

-- 复合索引: 查找会员即将过期的用户
CREATE INDEX IF NOT EXISTS idx_users_member_expiring ON nexus.users(member_expire_at) 
    WHERE member_expire_at IS NOT NULL AND member_expire_at > NOW();

-- -----------------------------------------------------
-- 4. 更新 users 表注释（补充说明）
-- -----------------------------------------------------
COMMENT ON COLUMN nexus.users.last_login_at IS '最后登录时间（每次成功登录后更新）';
COMMENT ON COLUMN nexus.users.last_login_ip IS '最后登录IP（每次成功登录后更新）';
