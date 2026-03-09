-- ==========================================
-- 1. 아테나 로그 및 유저 지수/페르소나 신규 테이블 생성
-- ==========================================

-- 1-1. 유저 이벤트 피처 테이블
CREATE TABLE IF NOT EXISTS user_event_features_7d (
    snapshot_date date NOT NULL,
    member_id     bigint NOT NULL,

    -- 7일 기준
    click_list_type_cnt        bigint NOT NULL DEFAULT 0,
    click_product_detail_cnt   bigint NOT NULL DEFAULT 0,
    click_compare_cnt          bigint NOT NULL DEFAULT 0,
    click_coupon_cnt           bigint NOT NULL DEFAULT 0,
    click_penalty_cnt          bigint NOT NULL DEFAULT 0,
    click_change_cnt           bigint NOT NULL DEFAULT 0,

    -- 90일 기준
    click_change_success_cnt   bigint NOT NULL DEFAULT 0,

    -- 7일 기준 (JSONB)
    product_type_clicks        jsonb  NOT NULL DEFAULT '{}'::jsonb,
    product_type_top_tags      jsonb  NOT NULL DEFAULT '{}'::jsonb,

    created_at timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (snapshot_date, member_id),

    CONSTRAINT fk_user_event_features_7d_member
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_event_features_7d_member ON user_event_features_7d (member_id);
CREATE INDEX IF NOT EXISTS idx_user_event_features_7d_snapshot ON user_event_features_7d (snapshot_date);

-- 1-2. 멤버 각 지수별 RAW 테이블
CREATE TABLE IF NOT EXISTS index_raw_snapshot (
      snapshot_date date NOT NULL,
      member_id bigint NOT NULL,
      explore_raw numeric NOT NULL,
      benefit_trend_raw numeric NOT NULL,
      multi_device_raw numeric NOT NULL,
      family_home_raw numeric NOT NULL,
      internet_security_raw numeric NOT NULL,
      stability_raw numeric NOT NULL,
      created_at timestamptz NOT NULL DEFAULT now(),
      CONSTRAINT pk_index_raw_snapshot PRIMARY KEY (snapshot_date, member_id),
      CONSTRAINT fk_index_raw_snapshot_member
          FOREIGN KEY (member_id) REFERENCES member (member_id)
          ON UPDATE CASCADE ON DELETE CASCADE
  );
CREATE INDEX IF NOT EXISTS idx_index_raw_snapshot_member ON index_raw_snapshot (member_id);

-- 1-3. 멤버별 각 Tscore 테이블
CREATE TABLE IF NOT EXISTS index_tscore_snapshot (
      snapshot_date date NOT NULL,
      member_id bigint NOT NULL,
      explore_tscore numeric NOT NULL,
      benefit_trend_tscore numeric NOT NULL,
      multi_device_tscore numeric NOT NULL,
      family_home_tscore numeric NOT NULL,
      internet_security_tscore numeric NOT NULL,
      stability_tscore numeric NOT NULL,
      created_at timestamptz NOT NULL DEFAULT now(),
      CONSTRAINT pk_index_tscore_snapshot PRIMARY KEY (snapshot_date, member_id),
      CONSTRAINT fk_index_tscore_snapshot_member
          FOREIGN KEY (member_id) REFERENCES member (member_id)
          ON UPDATE CASCADE ON DELETE CASCADE
  );
CREATE INDEX IF NOT EXISTS idx_index_tscore_snapshot_member ON index_tscore_snapshot (member_id);

-- 1-4. 멤버별 캐릭터(페르소나) 테이블
CREATE TABLE IF NOT EXISTS index_persona_snapshot (
      snapshot_date date NOT NULL,
      member_id bigint NOT NULL,
      persona_code varchar(50) NOT NULL,
      source_index_code varchar(50) NOT NULL,
      source_tscore numeric NOT NULL,
      created_at timestamptz NOT NULL DEFAULT now(),
      CONSTRAINT pk_index_persona_snapshot PRIMARY KEY (snapshot_date, member_id),
      CONSTRAINT fk_index_persona_snapshot_member
          FOREIGN KEY (member_id) REFERENCES member (member_id)
          ON UPDATE CASCADE
          ON DELETE CASCADE
  );
CREATE INDEX IF NOT EXISTS idx_index_persona_snapshot_member ON index_persona_snapshot (member_id);
CREATE INDEX IF NOT EXISTS idx_index_persona_snapshot_code ON index_persona_snapshot (persona_code);


-- ==========================================
-- 2. 상담 분석 및 아웃박스(Outbox) 테이블 변경
-- ==========================================

-- 2-1. 발송 아웃박스 상태 ENUM 추가
CREATE TYPE dispatch_status AS ENUM ('READY', 'SENT', 'ACKED', 'RETRY', 'DEAD');

-- 2-2. 상담 분석 내역 유니크 키 변경 (case_id -> case_id + analyzer_version)
ALTER TABLE consultation_analysis
    DROP CONSTRAINT IF EXISTS uk_case_id;

ALTER TABLE consultation_analysis
    ADD CONSTRAINT uk_case_version UNIQUE (case_id, analyzer_version);

-- 2-3. 발송 아웃박스 컬럼 추가 (상태 및 선점 로직 이관)
ALTER TABLE analysis_dispatch_outbox
  DROP COLUMN IF EXISTS status,
  ADD COLUMN IF NOT EXISTS claim_token BIGINT,
  ADD COLUMN IF NOT EXISTS claimed_started_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS claimed_done_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS analysis_status analysis_status,
  ADD COLUMN IF NOT EXISTS dispatch_status dispatch_status;

-- 2-4. 상담 분석 내역 불필요 컬럼 삭제 (아웃박스로 이관 완료)
ALTER TABLE consultation_analysis
  DROP COLUMN IF EXISTS status,
  DROP COLUMN IF EXISTS claim_token,
  DROP COLUMN IF EXISTS claimed_started_at,
  DROP COLUMN IF EXISTS claimed_done_at,
  DROP COLUMN IF EXISTS error_message,
  DROP COLUMN IF EXISTS analysis_status;