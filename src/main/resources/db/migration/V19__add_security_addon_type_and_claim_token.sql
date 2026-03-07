-- 1. 부가서비스 타입 ENUM에 'SECURITY' (보안) 추가
ALTER TYPE addon_type_enum ADD VALUE 'SECURITY';

-- 2. 상담 분석 내역 테이블에 선점 토큰(claim_token) 컬럼 추가
ALTER TABLE consultation_analysis
    ADD COLUMN claim_token BIGINT;

-- 3. 아웃박스 type 구분을 위한 새로운 ENUM 생성
CREATE TYPE dispatch_outbox_type AS ENUM ('REQUEST', 'RESPONSE');

-- 4. 아웃박스 테이블 컬럼 삭제 및 추가
ALTER TABLE analysis_dispatch_outbox
    DROP COLUMN efs_path_counsel,
    DROP COLUMN efs_path_alias,
    ADD COLUMN type dispatch_outbox_type NOT NULL;