-- 분석 요청 상태 관리 ENUM 타입
CREATE TYPE dispatch_outbox_status AS ENUM (
    'READY', 'SENT', 'ACKED', 'RETRY', 'DEAD'
);

-- 분석 요청 아웃박스 테이블
CREATE TABLE analysis_dispatch_outbox (
    request_id VARCHAR(100) NOT NULL,                  -- 발송 요청 id (멱등키)
    job_instance_id BIGINT NOT NULL,                   -- 배치 작업 인스턴스 id
    chunk_id VARCHAR(100) NOT NULL,                    -- 청크 id (예: 202603010001-0001)
    efs_path_counsel VARCHAR(255) NOT NULL,            -- 상담 데이터 EFS 경로
    efs_path_alias VARCHAR(255) NOT NULL,              -- 별칭 사전 EFS 경로
    analysis_version VARCHAR(50) NOT NULL,             -- 분석 알고리즘 버전
    status dispatch_outbox_status NOT NULL DEFAULT 'READY', -- 상태
    attempt_count INTEGER NOT NULL DEFAULT 0,          -- 발송 재시도 횟수
    next_retry_at TIMESTAMP,                           -- 다음 재시도 예정 시각
    last_error TEXT,                                   -- 마지막 발송 실패 사유

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_analysis_dispatch_outbox PRIMARY KEY (request_id)
);

-- 상담 분석 내역 테이블에 생성/수정일시 추가
ALTER TABLE consultation_analysis
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- 비즈니스 키워드 매핑 결과 테이블에 생성/수정일시 추가
ALTER TABLE business_keyword_mapping_result
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- 카테고리(대분류) 마스터 테이블에 생성/수정일시 추가
ALTER TABLE category_group
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- 카테고리(소분류) 마스터 테이블에 생성/수정일시 추가
ALTER TABLE category
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();