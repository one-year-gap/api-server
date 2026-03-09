DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'dispatch_status') THEN
    ALTER TYPE dispatch_status ADD VALUE IF NOT EXISTS 'READY';
    ALTER TYPE dispatch_status ADD VALUE IF NOT EXISTS 'SENT';
    ALTER TYPE dispatch_status ADD VALUE IF NOT EXISTS 'ACKED';
    ALTER TYPE dispatch_status ADD VALUE IF NOT EXISTS 'RETRY';
    ALTER TYPE dispatch_status ADD VALUE IF NOT EXISTS 'DEAD';
  ELSE
    CREATE TYPE dispatch_status AS ENUM ('READY', 'SENT', 'ACKED', 'RETRY', 'DEAD');
  END IF;
END
$$;

ALTER TABLE consultation_analysis
DROP CONSTRAINT IF EXISTS uk_case_id;

ALTER TABLE consultation_analysis
ADD CONSTRAINT uk_case_version UNIQUE (case_id, analyzer_version);

--- 발송 아웃박스
--- column 추가
ALTER TABLE analysis_dispatch_outbox
  DROP COLUMN IF EXISTS status,
  ADD COLUMN IF NOT EXISTS claim_token BIGINT,
  ADD COLUMN IF NOT EXISTS claimed_started_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS claimed_done_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS analysis_status analysis_status,
  ADD COLUMN IF NOT EXISTS dispatch_status  dispatch_status;

ALTER TABLE consultation_analysis
  DROP COLUMN IF EXISTS status,
  DROP COLUMN IF EXISTS claim_token,
  DROP COLUMN IF EXISTS claimed_started_at,
  DROP COLUMN IF EXISTS claimed_done_at,
  DROP COLUMN IF EXISTS error_message,
  DROP COLUMN IF EXISTS analysis_status;
