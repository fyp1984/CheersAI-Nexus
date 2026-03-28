-- =====================================================
-- CheersAI Nexus 会员服务数据库初始化脚本
-- Version: V1
-- 依赖: auth 服务的 V1__create_auth_tables.sql (需要 nexus.users 表存在)
-- 说明: 创建会员服务所需的基础表结构
--       - membership_plans: 会员计划表
--       - subscriptions: 用户订阅表
--       - plan_audit_logs: 会员计划变更审计日志
--       - subscription_audit_logs: 订阅变更审计日志
-- =====================================================

-- -----------------------------------------------------
-- 1. 会员计划表 (membership_plans)
-- 说明: 存储会员等级、权益和价格配置
--       PRD 定义: Free/Pro/Team/Enterprise 四个等级
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.membership_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(32) UNIQUE NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    price_monthly   DECIMAL(10, 2),
    price_yearly    DECIMAL(10, 2),
    currency        VARCHAR(8) DEFAULT 'CNY',
    features        JSONB DEFAULT '{}',
    limits          JSONB DEFAULT '{}',
    sort_order      INTEGER DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'disabled')),
    audit_status    VARCHAR(20) DEFAULT 'approved' CHECK (audit_status IN ('pending', 'approved', 'rejected')),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- 会员计划表索引
CREATE INDEX IF NOT EXISTS idx_membership_plans_code ON nexus.membership_plans(code);
CREATE INDEX IF NOT EXISTS idx_membership_plans_status ON nexus.membership_plans(status);
CREATE INDEX IF NOT EXISTS idx_membership_plans_sort_order ON nexus.membership_plans(sort_order);

-- 会员计划表注释
COMMENT ON TABLE nexus.membership_plans IS '会员计划表 - 存储会员等级配置';
COMMENT ON COLUMN nexus.membership_plans.code IS '计划代码(唯一): free/pro/team/enterprise';
COMMENT ON COLUMN nexus.membership_plans.name IS '计划名称';
COMMENT ON COLUMN nexus.membership_plans.description IS '计划描述';
COMMENT ON COLUMN nexus.membership_plans.price_monthly IS '月付价格(元)';
COMMENT ON COLUMN nexus.membership_plans.price_yearly IS '年付价格(元)';
COMMENT ON COLUMN nexus.membership_plans.currency IS '货币类型: CNY/USD';
COMMENT ON COLUMN nexus.membership_plans.features IS '权益配置(JSONB)';
COMMENT ON COLUMN nexus.membership_plans.limits IS '额度配置(JSONB)';
COMMENT ON COLUMN nexus.membership_plans.sort_order IS '排序顺序';
COMMENT ON COLUMN nexus.membership_plans.status IS '状态: active-启用, disabled-禁用';
COMMENT ON COLUMN nexus.membership_plans.audit_status IS '审计状态: pending-待审批, approved-已通过, rejected-已驳回';

-- -----------------------------------------------------
-- 2. 用户订阅表 (subscriptions)
-- 说明: 存储用户的会员订阅信息
--       支持订阅、续费、取消、自动续费等功能
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES nexus.users(id) ON DELETE CASCADE,
    plan_code       VARCHAR(32) NOT NULL REFERENCES nexus.membership_plans(code),
    status          VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'expired', 'cancelled')),
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    auto_renew      BOOLEAN DEFAULT true,
    payment_method  VARCHAR(32),
    last_payment_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_subscription_dates CHECK (end_date >= start_date)
);

-- 用户订阅表索引
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON nexus.subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_code ON nexus.subscriptions(plan_code);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON nexus.subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date ON nexus.subscriptions(end_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_auto_renew ON nexus.subscriptions(auto_renew);

-- 用户订阅表注释
COMMENT ON TABLE nexus.subscriptions IS '用户订阅表 - 存储用户会员订阅信息';
COMMENT ON COLUMN nexus.subscriptions.user_id IS '关联用户ID';
COMMENT ON COLUMN nexus.subscriptions.plan_code IS '会员计划代码';
COMMENT ON COLUMN nexus.subscriptions.status IS '订阅状态: active-生效中, expired-已过期, cancelled-已取消';
COMMENT ON COLUMN nexus.subscriptions.start_date IS '开始日期';
COMMENT ON COLUMN nexus.subscriptions.end_date IS '结束日期';
COMMENT ON COLUMN nexus.subscriptions.auto_renew IS '是否自动续费';
COMMENT ON COLUMN nexus.subscriptions.payment_method IS '支付方式';
COMMENT ON COLUMN nexus.subscriptions.last_payment_at IS '最后支付时间';

-- -----------------------------------------------------
-- 3. 会员计划变更审计日志表 (plan_audit_logs)
-- 说明: 记录会员计划的变更历史，支持审批流程
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.plan_audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id         UUID NOT NULL REFERENCES nexus.membership_plans(id) ON DELETE CASCADE,
    plan_code       VARCHAR(32) NOT NULL,
    operate_type    VARCHAR(32) NOT NULL CHECK (operate_type IN ('create', 'update', 'delete', 'status', 'benefit')),
    before_data     JSONB,
    after_data      JSONB,
    apply_remark    TEXT,
    applicant_id    VARCHAR(64),
    applicant_name  VARCHAR(100),
    audit_status    VARCHAR(20) DEFAULT 'pending' CHECK (audit_status IN ('pending', 'approved', 'rejected')),
    auditor_id      VARCHAR(64),
    auditor_name    VARCHAR(100),
    audit_remark    TEXT,
    applied_at      TIMESTAMP DEFAULT NOW(),
    audited_at      TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- 会员计划变更审计日志表索引
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_plan_id ON nexus.plan_audit_logs(plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_plan_code ON nexus.plan_audit_logs(plan_code);
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_operate_type ON nexus.plan_audit_logs(operate_type);
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_audit_status ON nexus.plan_audit_logs(audit_status);
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_applicant_id ON nexus.plan_audit_logs(applicant_id);
CREATE INDEX IF NOT EXISTS idx_plan_audit_logs_created_at ON nexus.plan_audit_logs(created_at DESC);

-- 会员计划变更审计日志表注释
COMMENT ON TABLE nexus.plan_audit_logs IS '会员计划变更审计日志表';
COMMENT ON COLUMN nexus.plan_audit_logs.plan_id IS '关联计划ID';
COMMENT ON COLUMN nexus.plan_audit_logs.plan_code IS '计划代码';
COMMENT ON COLUMN nexus.plan_audit_logs.operate_type IS '操作类型: create-创建, update-更新, delete-删除, status-状态变更, benefit-权益变更';
COMMENT ON COLUMN nexus.plan_audit_logs.before_data IS '变更前数据(JSON)';
COMMENT ON COLUMN nexus.plan_audit_logs.after_data IS '变更后数据(JSON)';
COMMENT ON COLUMN nexus.plan_audit_logs.apply_remark IS '申请备注';
COMMENT ON COLUMN nexus.plan_audit_logs.applicant_id IS '申请人ID';
COMMENT ON COLUMN nexus.plan_audit_logs.applicant_name IS '申请人名称';
COMMENT ON COLUMN nexus.plan_audit_logs.audit_status IS '审计状态: pending-待审批, approved-已通过, rejected-已驳回';
COMMENT ON COLUMN nexus.plan_audit_logs.auditor_id IS '审批人ID';
COMMENT ON COLUMN nexus.plan_audit_logs.auditor_name IS '审批人名称';
COMMENT ON COLUMN nexus.plan_audit_logs.audit_remark IS '审批备注';
COMMENT ON COLUMN nexus.plan_audit_logs.applied_at IS '申请时间';
COMMENT ON COLUMN nexus.plan_audit_logs.audited_at IS '审批时间';

-- -----------------------------------------------------
-- 4. 订阅变更审计日志表 (subscription_audit_logs)
-- 说明: 记录用户订阅的变更历史（开通、升级、续费、取消等）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS nexus.subscription_audit_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id     UUID REFERENCES nexus.subscriptions(id) ON DELETE SET NULL,
    user_id             UUID NOT NULL REFERENCES nexus.users(id) ON DELETE CASCADE,
    operate_type        VARCHAR(32) NOT NULL CHECK (operate_type IN ('create', 'upgrade', 'downgrade', 'renew', 'cancel', 'expire', 'adjust')),
    before_plan_code    VARCHAR(32),
    after_plan_code     VARCHAR(32),
    before_end_date     DATE,
    after_end_date      DATE,
    reason              TEXT,
    operator_id         VARCHAR(64),
    operator_name       VARCHAR(100),
    operator_ip         VARCHAR(64),
    created_at          TIMESTAMP DEFAULT NOW()
);

-- 订阅变更审计日志表索引
CREATE INDEX IF NOT EXISTS idx_subscription_audit_logs_subscription_id ON nexus.subscription_audit_logs(subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_logs_user_id ON nexus.subscription_audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_logs_operate_type ON nexus.subscription_audit_logs(operate_type);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_logs_created_at ON nexus.subscription_audit_logs(created_at DESC);

-- 订阅变更审计日志表注释
COMMENT ON TABLE nexus.subscription_audit_logs IS '订阅变更审计日志表';
COMMENT ON COLUMN nexus.subscription_audit_logs.subscription_id IS '关联订阅ID';
COMMENT ON COLUMN nexus.subscription_audit_logs.user_id IS '用户ID';
COMMENT ON COLUMN nexus.subscription_audit_logs.operate_type IS '操作类型: create-开通, upgrade-升级, downgrade-降级, renew-续费, cancel-取消, expire-过期, adjust-手动调整';
COMMENT ON COLUMN nexus.subscription_audit_logs.before_plan_code IS '变更前计划代码';
COMMENT ON COLUMN nexus.subscription_audit_logs.after_plan_code IS '变更后计划代码';
COMMENT ON COLUMN nexus.subscription_audit_logs.before_end_date IS '变更前到期日';
COMMENT ON COLUMN nexus.subscription_audit_logs.after_end_date IS '变更后到期日';
COMMENT ON COLUMN nexus.subscription_audit_logs.reason IS '变更原因';
COMMENT ON COLUMN nexus.subscription_audit_logs.operator_id IS '操作人ID';
COMMENT ON COLUMN nexus.subscription_audit_logs.operator_name IS '操作人名称';
COMMENT ON COLUMN nexus.subscription_audit_logs.operator_ip IS '操作人IP';

-- -----------------------------------------------------
-- 5. 初始化默认会员计划
-- 说明: 根据 PRD 定义初始化四个默认会员等级
--       Free/Pro/Team/Enterprise
-- -----------------------------------------------------

-- 插入 Free 会员计划
INSERT INTO nexus.membership_plans (code, name, description, price_monthly, price_yearly, currency, features, limits, sort_order, status, audit_status)
VALUES (
    'free',
    '免费版',
    '免费版，基础功能',
    0.00,
    0.00,
    'CNY',
    '{"agentCount": 3, "knowledgeBaseCount": 1, "fileDesensitization": "10次/天", "apiCalls": "100次/天", "dataStorage": "1GB", "teamMembers": 1, "prioritySupport": false, "privateDeployment": false}',
    '{"agentCount": 3, "knowledgeBaseCount": 1, "fileDesensitizationDaily": 10, "apiCallsDaily": 100, "dataStorageGB": 1, "teamMembers": 1}',
    0,
    'active',
    'approved'
) ON CONFLICT (code) DO NOTHING;

-- 插入 Pro 会员计划
INSERT INTO nexus.membership_plans (code, name, description, price_monthly, price_yearly, currency, features, limits, sort_order, status, audit_status)
VALUES (
    'pro',
    '专业版',
    '专业版，高级功能',
    29.00,
    299.00,
    'CNY',
    '{"agentCount": 20, "knowledgeBaseCount": 10, "fileDesensitization": "100次/天", "apiCalls": "1000次/天", "dataStorage": "10GB", "teamMembers": 1, "prioritySupport": true, "privateDeployment": false}',
    '{"agentCount": 20, "knowledgeBaseCount": 10, "fileDesensitizationDaily": 100, "apiCallsDaily": 1000, "dataStorageGB": 10, "teamMembers": 1}',
    1,
    'active',
    'approved'
) ON CONFLICT (code) DO NOTHING;

-- 插入 Team 会员计划
INSERT INTO nexus.membership_plans (code, name, description, price_monthly, price_yearly, currency, features, limits, sort_order, status, audit_status)
VALUES (
    'team',
    '团队版',
    '团队版，协作功能',
    99.00,
    999.00,
    'CNY',
    '{"agentCount": 100, "knowledgeBaseCount": 50, "fileDesensitization": "500次/天", "apiCalls": "5000次/天", "dataStorage": "100GB", "teamMembers": 10, "prioritySupport": true, "privateDeployment": false}',
    '{"agentCount": 100, "knowledgeBaseCount": 50, "fileDesensitizationDaily": 500, "apiCallsDaily": 5000, "dataStorageGB": 100, "teamMembers": 10}',
    2,
    'active',
    'approved'
) ON CONFLICT (code) DO NOTHING;

-- 插入 Enterprise 会员计划
INSERT INTO nexus.membership_plans (code, name, description, price_monthly, price_yearly, currency, features, limits, sort_order, status, audit_status)
VALUES (
    'enterprise',
    '企业版',
    '企业版，私有化部署',
    0.00,
    0.00,
    'CNY',
    '{"agentCount": -1, "knowledgeBaseCount": -1, "fileDesensitization": "无限", "apiCalls": "无限", "dataStorage": "无限", "teamMembers": -1, "prioritySupport": true, "privateDeployment": true}',
    '{"agentCount": -1, "knowledgeBaseCount": -1, "fileDesensitizationDaily": -1, "apiCallsDaily": -1, "dataStorageGB": -1, "teamMembers": -1}',
    3,
    'active',
    'approved'
) ON CONFLICT (code) DO NOTHING;
