-- 약정 개월 수 (12, 24 / 무약정이면 NULL)
ALTER TABLE subscription
ADD COLUMN contract_months INTEGER;

-- 약정 만료일
ALTER TABLE subscription
ADD COLUMN contract_end_date TIMESTAMP;