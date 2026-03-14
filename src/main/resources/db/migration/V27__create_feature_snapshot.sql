-- enum 생성
CREATE TYPE feature_type AS ENUM (
    'CONTRACT_FEATURE',
    'USAGE_FEATURE',
    'CHANGE_FREQ_FEATURE',
    'COUNSEL_SATIS_'
);

-- Feature store 생성
CREATE TABLE feature_snapshot_store (
    feature_snapshot_id BIGINT PRIMARY KEY,
    member_id           BIGINT NOT NULL ,
    feature_type        feature_type NOT NULL ,
    created_at          TIMESTAMP NOT NULL ,
    updated_at          TIMESTAMP NOT NULL ,

    -- 외래 키 제약
    CONSTRAINT fk_feature_snapshot_member
                FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 1.계약 기반 feature
CREATE TABLE contract_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY ,
    contract_remaining_weeks    SMALLINT NOT NULL,
    tenure_weeks                INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_contract_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_contract_remaining_weeks
        -- 약정 기간: 최대 2년 기준
        CHECK (contract_remaining_weeks BETWEEN 0 AND 106),

    CONSTRAINT chk_tenure_weeks
        CHECK (tenure_weeks >= 0)
);


-- 2.사용량 기반 feature
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

-- 3. 요금제 변경 이력 feature
CREATE TABLE change_freq_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    change_mobile_cnt           SMALLINT NOT NULL ,

    CONSTRAINT fk_change_freq_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE
);

-- 4. 상담 만족도 평균 feature
CREATE TABLE consultation_satisfaction_feature (
    feature_snapshot_id         BIGINT PRIMARY KEY,
    star_mean_score             NUMERIC(2,1) NOT NULL,

        CONSTRAINT fk_consultation_satisfaction_freq_feature_snapshot
        FOREIGN KEY (feature_snapshot_id)
        REFERENCES feature_snapshot_store(feature_snapshot_id)
        ON DELETE CASCADE
);
