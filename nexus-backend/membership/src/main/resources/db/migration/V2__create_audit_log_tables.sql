-- 会员管理模块审计日志表
-- Version: V2__create_audit_log_tables.sql

-- 会员计划审批/变更记录表
CREATE TABLE plan_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID,
    operate_type VARCHAR(20) NOT NULL CHECK (operate_type IN ('create', 'update', 'delete', 'status', 'benefit')),
    audit_status VARCHAR(20) DEFAULT 'pending' CHECK (audit_status IN ('pending', 'approved', 'rejected')),
    before_data JSONB,
    after_data JSONB,
    auditor_id UUID,
    auditor_name VARCHAR(100),
    audit_remark TEXT,
    audited_at TIMESTAMP,
    applicant_id UUID,
    applicant_name VARCHAR(100),
    apply_remark TEXT,
    applied_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 用户订阅变更审计日志表
CREATE TABLE subscription_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL,
    user_id UUID NOT NULL,
    operate_type VARCHAR(20) NOT NULL CHECK (operate_type IN ('adjust', 'upgrade', 'downgrade', 'extend', 'shorten')),
    before_plan_code VARCHAR(50),
    after_plan_code VARCHAR(50),
    before_end_date VARCHAR(20),
    after_end_date VARCHAR(20),
    reason TEXT,
    operator_id UUID NOT NULL,
    operator_name VARCHAR(100),
    operator_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_plan_audit_logs_plan_id ON plan_audit_logs(plan_id);
CREATE INDEX idx_plan_audit_logs_operate_type ON plan_audit_logs(operate_type);
CREATE INDEX idx_plan_audit_logs_audit_status ON plan_audit_logs(audit_status);
CREATE INDEX idx_plan_audit_logs_applicant_id ON plan_audit_logs(applicant_id);
CREATE INDEX idx_plan_audit_logs_auditor_id ON plan_audit_logs(auditor_id);
CREATE INDEX idx_plan_audit_logs_applied_at ON plan_audit_logs(applied_at);
CREATE INDEX idx_subscription_audit_logs_subscription_id ON subscription_audit_logs(subscription_id);
CREATE INDEX idx_subscription_audit_logs_user_id ON subscription_audit_logs(user_id);
CREATE INDEX idx_subscription_audit_logs_operate_type ON subscription_audit_logs(operate_type);
CREATE INDEX idx_subscription_audit_logs_created_at ON subscription_audit_logs(created_at);

-- 注释
COMMENT ON TABLE plan_audit_logs IS '会员计划审批/变更记录表';
COMMENT ON TABLE subscription_audit_logs IS '用户订阅变更审计日志表';
COMMENT ON COLUMN plan_audit_logs.operate_type IS '操作类型: create-创建, update-更新, delete-删除, status-状态变更, benefit-权益配置';
COMMENT ON COLUMN plan_audit_logs.audit_status IS '审批状态: pending-待审批, approved-已通过, rejected-已驳回';
COMMENT ON COLUMN subscription_audit_logs.operate_type IS '操作类型: adjust-手动调整, upgrade-升级, downgrade-降级, extend-延长, shorten-缩短';
