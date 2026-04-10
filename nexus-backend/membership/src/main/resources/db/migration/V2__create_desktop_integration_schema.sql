-- =====================================================
-- Desktop Integration Schema (migrated from desktop-integration module)
-- Tables: api_keys, desktop_members, desktop_member_events
-- =====================================================

-- API Keys table for external API authentication
CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_id VARCHAR(64) NOT NULL UNIQUE,
    key_secret_hash VARCHAR(256) NOT NULL,
    name VARCHAR(128) NOT NULL,
    permissions JSONB DEFAULT '[]',
    rate_limit INTEGER DEFAULT 1000,
    status VARCHAR(32) DEFAULT 'active',
    created_by UUID,
    created_at TIMESTAMP DEFAULT now(),
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_keys_key_id ON api_keys(key_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_status ON api_keys(status);

-- Desktop Members table (synced from Desktop SSO/Casdoor)
CREATE TABLE IF NOT EXISTS desktop_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sso_user_id VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(256),
    name VARCHAR(256),
    avatar_url VARCHAR(512),
    status VARCHAR(32) DEFAULT 'active',
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(64),
    last_active_at TIMESTAMP,
    device_info JSONB DEFAULT '{}',
    app_version VARCHAR(32),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_desktop_members_sso_user_id ON desktop_members(sso_user_id);
CREATE INDEX IF NOT EXISTS idx_desktop_members_status ON desktop_members(status);
CREATE INDEX IF NOT EXISTS idx_desktop_members_last_login_at ON desktop_members(last_login_at);

-- Desktop Member Events table
CREATE TABLE IF NOT EXISTS desktop_member_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL REFERENCES desktop_members(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    event_data JSONB DEFAULT '{}',
    session_id VARCHAR(128),
    ip_address VARCHAR(64),
    user_agent TEXT,
    device_info JSONB DEFAULT '{}',
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_desktop_member_events_member_id ON desktop_member_events(member_id);
CREATE INDEX IF NOT EXISTS idx_desktop_member_events_event_type ON desktop_member_events(event_type);
CREATE INDEX IF NOT EXISTS idx_desktop_member_events_occurred_at ON desktop_member_events(occurred_at);
