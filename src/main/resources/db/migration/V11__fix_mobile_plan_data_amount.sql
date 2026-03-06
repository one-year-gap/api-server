-- V__normalize_mobile_plan_data_amount.sql

-- 0) 삭제 - 
DELETE FROM mobile_plan
WHERE product_id = 28;

-- 1) 무제한 정규화 (어떤 형태든 최종은 '무제한')
UPDATE mobile_plan
SET data_amount = '무제한'
WHERE data_amount LIKE '%무제한%';

-- 2) 표현 통일: '데이터 일 X' -> '매일 X'
UPDATE mobile_plan
SET data_amount = REPLACE(data_amount, '데이터 일', '매일')
WHERE data_amount LIKE '데이터 일%';

-- 3) 표현 통일: '데이터 매일 X' -> '매일 X'
UPDATE mobile_plan
SET data_amount = REPLACE(data_amount, '데이터 매일', '매일')
WHERE data_amount LIKE '데이터 매일%';

-- 4) MB -> GB 변환 (1024MB = 1GB, 소수점 3자리)
-- '데이터 ' prefix가 있든 없든 substring으로 숫자만 뽑아서 변환
-- 단, product_id=48은 아래에서 1GB로 강제 변경하므로 제외
UPDATE mobile_plan
SET data_amount =
    trim(to_char(
        round(
            (substring(data_amount from '([0-9]+(?:\.[0-9]+)?)')::numeric) / 1024.0
        , 3)
    , 'FM999999990.000')) || 'GB'
WHERE data_amount ~ 'MB'
  AND product_id <> 48;

-- 5) 남아있는 "데이터 " prefix 제거 (무제한/매일 변환 이후 정리)
UPDATE mobile_plan
SET data_amount = REPLACE(data_amount, '데이터 ', '')
WHERE data_amount LIKE '데이터 %';

-- 6) 특정 상품 강제 보정 
UPDATE mobile_plan
SET data_amount = '매일 2GB'
WHERE product_id = 35;

UPDATE mobile_plan
SET data_amount = '1GB'
WHERE product_id = 48;

UPDATE mobile_plan
SET data_amount = '매일 5GB'
WHERE product_id = 33;