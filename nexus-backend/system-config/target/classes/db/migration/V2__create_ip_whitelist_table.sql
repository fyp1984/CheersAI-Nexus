-- IP Whitelist Table
-- Stores trusted IP addresses for system access control

CREATE TABLE IF NOT EXISTS ip_whitelist (
                                            id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            ip_address  VARCHAR(45) NOT NULL,
                                            remark      VARCHAR(256),
                                            created_by  VARCHAR(128),
                                            created_at  TIMESTAMP DEFAULT now(),
    -- 修正：PostgreSQL 标准唯一约束语法
                                            CONSTRAINT uk_ip_address UNIQUE (ip_address)
);

-- 修正：单独添加字段注释（PostgreSQL 标准写法）
COMMENT ON COLUMN ip_whitelist.ip_address IS 'IP地址（支持IPv4/IPv6）';
COMMENT ON COLUMN ip_whitelist.remark IS '备注';
COMMENT ON COLUMN ip_whitelist.created_by IS '创建人';
COMMENT ON COLUMN ip_whitelist.created_at IS '创建时间';

-- Create index（这行语法完全正确，无需修改）
CREATE INDEX idx_ip_whitelist_created_at ON ip_whitelist(created_at);