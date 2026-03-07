-- 1. 부가서비스 타입 ENUM에 'SECURITY' (보안) 추가
ALTER TYPE addon_type_enum ADD VALUE 'SECURITY';

-- 2. 상담 분석 내역 테이블에 선점 토큰(claim_token) 컬럼 추가
ALTER TABLE consultation_analysis
    ADD COLUMN claim_token BIGINT;