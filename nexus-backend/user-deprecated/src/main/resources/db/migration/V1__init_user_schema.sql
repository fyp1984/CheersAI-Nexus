-- CheersAI Nexus 用户管理模块数据库初始化脚本
-- V1__init_user_schema.sql
-- PostgreSQL 17.x

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'deleted')),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_email UNIQUE (email),
    CONSTRAINT uk_phone UNIQUE (phone),
    CONSTRAINT uk_username UNIQUE (username)
);

-- 用户索引
CREATE INDEX idx_user_email ON sys_user(email);
CREATE INDEX idx_user_phone ON sys_user(phone);
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_created_at ON sys_user(created_at);

-- 验证码表
CREATE TABLE IF NOT EXISTS sys_verify_code (
    id BIGSERIAL PRIMARY KEY,
    target VARCHAR(255) NOT NULL,
    code VARCHAR(8) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('email', 'phone')),
    purpose VARCHAR(20) NOT NULL CHECK (purpose IN ('register', 'reset_password', 'login')),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_verify_code_target (target),
    INDEX idx_verify_code_expires (expires_at)
);

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS sys_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE,
    INDEX idx_refresh_token_user_id (user_id),
    INDEX idx_refresh_token_token (token),
    INDEX idx_refresh_token_expires (expires_at)
);

-- 注释
COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.user_id IS '用户唯一标识';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.avatar_url IS '头像URL';
COMMENT ON COLUMN sys_user.password_hash IS '密码哈希';
COMMENT ON COLUMN sys_user.status IS '状态(active/inactive/deleted)';
COMMENT ON COLUMN sys_user.email_verified IS '邮箱是否验证';
COMMENT ON COLUMN sys_user.phone_verified IS '手机是否验证';
COMMENT ON COLUMN sys_user.last_login_at IS '最后登录时间';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';

COMMENT ON TABLE sys_verify_code IS '验证码表';
COMMENT ON COLUMN sys_verify_code.target IS '验证码目标(邮箱/手机号)';
COMMENT ON COLUMN sys_verify_code.code IS '验证码';
COMMENT ON COLUMN sys_verify_code.type IS '类型(email/phone)';
COMMENT ON COLUMN sys_verify_code.purpose IS '用途(register/reset_password/login)';
COMMENT ON COLUMN sys_verify_code.expires_at IS '过期时间';

COMMENT ON TABLE sys_refresh_token IS '刷新令牌表';
