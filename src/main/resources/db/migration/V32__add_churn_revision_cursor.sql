CREATE SEQUENCE IF NOT EXISTS churn_score_revision_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE churn_score_snapshot
    ADD COLUMN IF NOT EXISTS revision_id BIGINT NOT NULL DEFAULT nextval('churn_score_revision_seq'),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS uq_churn_score_snapshot_revision_id
    ON churn_score_snapshot (revision_id);

CREATE INDEX IF NOT EXISTS idx_churn_score_snapshot_revision_id
    ON churn_score_snapshot (revision_id DESC);

CREATE INDEX IF NOT EXISTS idx_churn_score_snapshot_updated_at
    ON churn_score_snapshot (updated_at DESC);

COMMENT ON COLUMN churn_score_snapshot.revision_id IS '실시간 변경 조회용 증가 커서';
COMMENT ON COLUMN churn_score_snapshot.updated_at IS '최근 갱신 시각';
