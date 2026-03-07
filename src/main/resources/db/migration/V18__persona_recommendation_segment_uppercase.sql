-- 1) 먼저 segment CHECK 제약 제거 (제약이 있는 상태에서는 대문자로 UPDATE 불가)
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attnum = ANY(c.conkey) AND a.attrelid = c.conrelid
        WHERE c.conrelid = 'persona_recommendation'::regclass
          AND c.contype = 'c'
          AND a.attname = 'segment'
    LOOP
        EXECUTE format('ALTER TABLE persona_recommendation DROP CONSTRAINT %I', r.conname);
    END LOOP;
END $$;

-- 2) 기존 데이터를 대문자로 변경
UPDATE persona_recommendation
SET segment = CASE segment
    WHEN 'upsell' THEN 'UPSELL'
    WHEN 'churn_risk' THEN 'CHURN_RISK'
    WHEN 'normal' THEN 'NORMAL'
    ELSE segment
END
WHERE segment IN ('upsell', 'churn_risk', 'normal');

-- 3) 대문자만 허용하는 CHECK 제약 추가
ALTER TABLE persona_recommendation
    ADD CONSTRAINT persona_recommendation_segment_check
    CHECK (segment IN ('UPSELL', 'CHURN_RISK', 'NORMAL'));
