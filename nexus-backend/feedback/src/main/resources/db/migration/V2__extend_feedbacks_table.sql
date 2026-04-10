-- Extend existing feedbacks table for external feedback support
-- (migrated from desktop-integration module V2__extend_feedbacks_table.sql)
ALTER TABLE nexus.feedbacks
ADD COLUMN IF NOT EXISTS desktop_member_id UUID,
ADD COLUMN IF NOT EXISTS source VARCHAR(32) DEFAULT 'internal',
ADD COLUMN IF NOT EXISTS external_user_info JSONB DEFAULT '{}';

CREATE INDEX IF NOT EXISTS idx_feedbacks_desktop_member_id ON nexus.feedbacks(desktop_member_id);
CREATE INDEX IF NOT EXISTS idx_feedbacks_source ON nexus.feedbacks(source);
