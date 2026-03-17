-- 이탈 상세 점수 (스냅샷당 1행, 세부 점수 분해)
CREATE TABLE churn_feature_score (
    snapshot_id         BIGINT    NOT NULL,
    churn_base_score    SMALLINT,
    churn_usage_score   SMALLINT,
    churn_counsel_score SMALLINT,
    churn_log_score     SMALLINT,

    CONSTRAINT pk_churn_feature_score PRIMARY KEY (snapshot_id),
    CONSTRAINT fk_churn_feature_score_snapshot
        FOREIGN KEY (snapshot_id) REFERENCES churn_score_snapshot (snapshot_id)
        ON DELETE CASCADE
);

ALTER TABLE member_dissatisfaction_feature
  ADD COLUMN IF NOT EXISTS negative_counsel_cnt INTEGER NOT NULL DEFAULT 0;

ALTER TABLE member_dissatisfaction_feature
  ADD COLUMN IF NOT EXISTS negative_keyword_cnt INTEGER NOT NULL DEFAULT 0;
