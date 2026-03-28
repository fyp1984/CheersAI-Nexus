-- IP Whitelist Table
-- Stores trusted IP addresses for system access control

CREATE TABLE IF NOT EXISTS ip_whitelist (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ip_address  VARCHAR(45) NOT NULL COMMENT 'IP地址（支持IPv4/IPv6）',
    remark      VARCHAR(256) COMMENT '备注',
    created_by  VARCHAR(128) COMMENT '创建人',
    created_at  TIMESTAMP DEFAULT now(),
    UNIQUE KEY uk_ip_address (ip_address)
);

-- Create index
CREATE INDEX idx_ip_whitelist_created_at ON ip_whitelist(created_at);
