-- 产品管理模块数据库表结构
-- Version: V1__create_product_tables.sql

-- 产品表
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'deprecated')),
    current_version VARCHAR(20),
    download_urls JSONB,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 产品版本表
CREATE TABLE product_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL,
    version_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'deprecated')),
    changelog TEXT,
    download_urls JSONB,
    release_note TEXT,
    force_update BOOLEAN DEFAULT false,
    min_version VARCHAR(20),
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(50),
    created_by_name VARCHAR(100),
    UNIQUE(product_id, version)
);

-- 创建索引
CREATE INDEX idx_products_code ON products(code);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_product_versions_product_id ON product_versions(product_id);
CREATE INDEX idx_product_versions_status ON product_versions(status);
CREATE INDEX idx_product_versions_published_at ON product_versions(published_at);

-- 注释
COMMENT ON TABLE products IS '产品表';
COMMENT ON TABLE product_versions IS '产品版本表';
COMMENT ON COLUMN products.status IS '产品状态: active-启用, inactive-停用, deprecated-已废弃';
COMMENT ON COLUMN products.download_urls IS '下载地址: JSONB格式 [{"platform": "windows", "url": "https://...", "version": "1.0.0"}]';
COMMENT ON COLUMN products.settings IS '产品设置: JSONB格式 {"featureFlags": {"darkMode": true}}';
COMMENT ON COLUMN product_versions.status IS '版本状态: draft-草稿, published-已发布, deprecated-已废弃';
COMMENT ON COLUMN product_versions.force_update IS '是否强制更新';
