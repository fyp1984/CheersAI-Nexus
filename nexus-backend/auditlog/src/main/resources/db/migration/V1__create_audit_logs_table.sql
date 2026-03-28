-- 审计日志模块数据库表结构
-- Version: V1__create_audit_logs_table.sql

-- 审计日志表
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    log_type VARCHAR(30) NOT NULL CHECK (log_type IN ('user_action', 'admin_action', 'system_event', 'security_event')),
    action VARCHAR(100) NOT NULL,
    operator_id UUID,
    operator_name VARCHAR(100),
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    before_data JSONB,
    after_data JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    result VARCHAR(20) DEFAULT 'success' CHECK (result IN ('success', 'failure')),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_audit_logs_log_type ON audit_logs(log_type);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_operator_id ON audit_logs(operator_id);
CREATE INDEX idx_audit_logs_target_type ON audit_logs(target_type);
CREATE INDEX idx_audit_logs_target_id ON audit_logs(target_id);
CREATE INDEX idx_audit_logs_result ON audit_logs(result);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);

-- 注释
COMMENT ON TABLE audit_logs IS '审计日志表';
COMMENT ON COLUMN audit_logs.log_type IS '日志类型: user_action-用户行为, admin_action-管理操作, system_event-系统事件, security_event-安全事件';
COMMENT ON COLUMN audit_logs.action IS '操作类型: 如 login, logout, create_plan, update_subscription 等';
COMMENT ON COLUMN audit_logs.target_type IS '目标类型: 如 User, Subscription, MembershipPlan 等';
COMMENT ON COLUMN audit_logs.result IS '操作结果: success-成功, failure-失败';
