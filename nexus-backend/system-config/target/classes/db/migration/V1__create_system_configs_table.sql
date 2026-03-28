-- System Configs Table
-- Stores system configuration for register, login, security, and token settings

CREATE TABLE IF NOT EXISTS system_configs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_category  VARCHAR(64) NOT NULL COMMENT '配置类别：register/login/security/token',
    config_key      VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value    TEXT NOT NULL COMMENT '配置值（JSON格式）',
    config_desc     VARCHAR(512) COMMENT '配置描述',
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now(),
    UNIQUE KEY uk_category_key (config_category, config_key)
);

-- Create indexes
CREATE INDEX idx_system_configs_category ON system_configs(config_category);
CREATE INDEX idx_system_configs_key ON system_configs(config_key);

-- Insert default configurations
INSERT INTO system_configs (config_category, config_key, config_value, config_desc) VALUES
-- Register Config Defaults
('register', 'register_methods', '["邮箱", "手机号", "企业邀请码"]', '允许的注册方式'),
('register', 'force_email_verify', 'false', '是否强制邮箱验证'),
('register', 'need_invite_code', 'false', '是否启用邀请码'),
('register', 'default_member_plan', '免费版', '默认会员方案'),
('register', 'auto_activate', 'true', '新用户自动激活'),

-- Login/Security Config Defaults
('security', 'login_mode', '"账号密码"', '登录方式'),
('security', 'enable_captcha', 'true', '登录验证码开关'),
('security', 'fail_lock_threshold', '5', '连续失败锁定阈值'),
('security', 'lock_minutes', '30', '锁定时长（分钟）'),
('security', 'enable_2fa', 'false', '双因子认证'),
('security', 'password_policy', '["至少 8 位", "包含大写字母", "包含数字", "包含特殊字符"]', '密码复杂度策略'),

-- Token Config Defaults
('token', 'access_token_hours', '2', 'Access Token 有效期（小时）'),
('token', 'refresh_token_days', '7', 'Refresh Token 有效期（天）'),
('token', 'token_issuer', '"CheersAI-Nexus"', 'Token 发行方'),
('token', 'allow_concurrent_sessions', 'false', '是否允许并发会话');
