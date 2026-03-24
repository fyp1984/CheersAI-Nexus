CREATE TABLE IF NOT EXISTS product_operation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID,
    product_code VARCHAR(50),
    product_name VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(64),
    content TEXT,
    before_data JSONB,
    after_data JSONB,
    operator_id VARCHAR(64),
    operator_name VARCHAR(100),
    ip_address VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_operation_logs_product_id ON product_operation_logs(product_id);
CREATE INDEX IF NOT EXISTS idx_product_operation_logs_created_at ON product_operation_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_product_operation_logs_action ON product_operation_logs(action);

COMMENT ON TABLE product_operation_logs IS '产品模块操作日志';
COMMENT ON COLUMN product_operation_logs.target_type IS '目标类型: product/version/features';
