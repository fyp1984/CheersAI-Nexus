CREATE TABLE IF NOT EXISTS feedbacks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    product_id      UUID,
    type            VARCHAR(32),
    title           VARCHAR(512),
    content         TEXT,
    attachments     JSONB DEFAULT '[]',
    status          VARCHAR(32) DEFAULT 'pending',
    priority        VARCHAR(32) DEFAULT 'medium',
    assignee_id     UUID,
    resolved_at     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now()
);
