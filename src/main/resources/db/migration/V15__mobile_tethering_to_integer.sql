-- 1) 문자열/정수 상관없이, 숫자만 남긴 뒤 INTEGER로 정규화
UPDATE mobile_plan
SET tethering_sharing_data =
        regexp_replace(tethering_sharing_data::text, '[^0-9]', '', 'g')::integer
WHERE tethering_sharing_data IS NOT NULL
  AND regexp_replace(tethering_sharing_data::text, '[^0-9]', '', 'g') ~ '\d';

-- 2) 컬럼 타입을 INTEGER로 변경 (이미 INTEGER인 환경에서는 사실상 no-op)
ALTER TABLE mobile_plan
ALTER COLUMN tethering_sharing_data TYPE INTEGER
    USING tethering_sharing_data::integer;