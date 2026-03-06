-- 기존 단일 처리 시간 컬럼 삭제
ALTER TABLE consultation_analysis
    DROP COLUMN processed_at;

-- 선점 시작/종료 시간 컬럼 신규 추가
ALTER TABLE consultation_analysis
    ADD COLUMN claimed_started_at TIMESTAMP,
    ADD COLUMN claimed_done_at TIMESTAMP;

-- case_id와 analyzer_version을 묶어서 복합 유니크 키 생성
ALTER TABLE consultation_analysis
    ADD CONSTRAINT uk_case_id UNIQUE (case_id);