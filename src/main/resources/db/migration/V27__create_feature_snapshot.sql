-- enum 생성
CREATE TYPE feature_type AS ENUM (
    'CONTRACT_FEATURE',
    'DISSATISFACTION_FEATURE',
    'USAGE_FEATURE',
    'MEMBER_ACTION_FEATURE'
);

-- feature snapshot 저장소
CREATE TABLE feature_snapshot_store (
    feature_snapshot_id BIGINT PRIMARY KEY,
    member_id           BIGINT NOT NULL,
    feature_type        feature_type NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    feature_score       INTEGER NOT NULL,

    CONSTRAINT fk_feature_snapshot_member
        FOREIGN KEY (member_id) REFERENCES member(member_id),

    CONSTRAINT chk_feature_score
        CHECK (feature_score BETWEEN 0 AND 100)
);

-- 1. 계약 기반 feature
CREATE TABLE contract_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    contract_remaining_weeks    SMALLINT NOT NULL,
    tenure_weeks                INTEGER NOT NULL,

    CONSTRAINT fk_contract_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_contract_remaining_weeks
        CHECK (contract_remaining_weeks BETWEEN 0 AND 106),

    CONSTRAINT chk_tenure_weeks
        CHECK (tenure_weeks >= 0)
);

-- 2. 사용량 기반 feature
CREATE TABLE usage_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    allowance_usage_rate_pct    INTEGER NOT NULL,

    CONSTRAINT fk_usage_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_allowance_usage_rate_pct
        CHECK (allowance_usage_rate_pct BETWEEN 0 AND 100)
);

-- 3. 유저 행동 기반 feature
CREATE TABLE member_action_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    change_mobile_cnt           SMALLINT NOT NULL DEFAULT 0,
    comparison_cnt              INTEGER NOT NULL DEFAULT 0,
    checked_penalty_fee_cnt     INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_member_action_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_change_mobile_cnt
        CHECK (change_mobile_cnt >= 0),

    CONSTRAINT chk_comparison_cnt
        CHECK (comparison_cnt >= 0),

    CONSTRAINT chk_checked_penalty_fee_cnt
        CHECK (checked_penalty_fee_cnt >= 0)
);

-- 4. 고객 불만 기반 feature
CREATE TABLE member_dissatisfaction_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    star_mean_score             NUMERIC(2,1) NOT NULL DEFAULT 0.0,
    negative_cnt                INTEGER NOT NULL DEFAULT 0,
    terminating_keyword_cnt     JSONB NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_member_dissatisfaction_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_star_mean_score
        CHECK (star_mean_score BETWEEN 0.0 AND 5.0),

    CONSTRAINT chk_negative_cnt
        CHECK (negative_cnt >= 0)
);


ALTER TABLE business_keyword
  ADD COLUMN IF NOT EXISTS negative_weight BIGINT NOT NULL DEFAULT 0;
