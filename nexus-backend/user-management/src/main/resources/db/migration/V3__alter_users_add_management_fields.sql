ALTER TABLE nexus.users
    ADD COLUMN IF NOT EXISTS role VARCHAR(32) DEFAULT 'user';

ALTER TABLE nexus.users
    ADD COLUMN IF NOT EXISTS member_plan_code VARCHAR(32) DEFAULT 'free';

ALTER TABLE nexus.users
    ADD COLUMN IF NOT EXISTS member_expire_at TIMESTAMP;

UPDATE nexus.users
SET role = COALESCE(role, 'user'),
    member_plan_code = COALESCE(member_plan_code, 'free')
WHERE role IS NULL
   OR member_plan_code IS NULL;

