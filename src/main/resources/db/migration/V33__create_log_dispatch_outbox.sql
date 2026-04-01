CREATE TYPE user_log_dispatch_status AS ENUM (
    'READY',
    'PROCESSING',
    'ACKED',
    'RETRY',
    'DEAD'
);

CREATE TABLE user_log_admin_dispatch_outbox (
    event_id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    event_name VARCHAR(200) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_timestamp TIMESTAMPTZ NOT NULL,
    payload JSONB NOT NULL,

    status user_log_dispatch_status NOT NULL DEFAULT 'READY',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_log_admin_dispatch_outbox_status_retry
ON user_log_admin_dispatch_outbox (status, next_retry_at, created_at);
