-- 会员管理模块数据库表结构
-- Version: V1__create_membership_tables.sql

-- 会员计划表
CREATE TABLE membership_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price_monthly DECIMAL(10, 2),
    price_yearly DECIMAL(10, 2),
    currency VARCHAR(10) DEFAULT 'CNY',
    features JSONB DEFAULT '{}',
    limits JSONB DEFAULT '{}',
    sort_order INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'disabled')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 用户订阅表
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'expired', 'cancelled')),
    start_date DATE,
    end_date DATE,
    auto_renew BOOLEAN DEFAULT false,
    payment_method VARCHAR(50),
    last_payment_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_membership_plans_code ON membership_plans(code);
CREATE INDEX idx_membership_plans_status ON membership_plans(status);
CREATE INDEX idx_membership_plans_sort_order ON membership_plans(sort_order);
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_plan_code ON subscriptions(plan_code);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- 注释
COMMENT ON TABLE membership_plans IS '会员计划表';
COMMENT ON TABLE subscriptions IS '用户订阅表';
COMMENT ON COLUMN membership_plans.features IS '权益配置: JSONB格式 {"aiChats": 1000, "productLimit": 5}';
COMMENT ON COLUMN membership_plans.limits IS '额度配置: JSONB格式 {"chatsPerMonth": 100, "productsCanCreate": 10}';
COMMENT ON COLUMN membership_plans.status IS '计划状态: active-启用, disabled-禁用';
COMMENT ON COLUMN subscriptions.status IS '订阅状态: active-生效中, expired-已过期, cancelled-已取消';
COMMENT ON COLUMN subscriptions.auto_renew IS '是否自动续费';
