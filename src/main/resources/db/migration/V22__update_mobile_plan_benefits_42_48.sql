-- product_id 42: benefit_voice_call → 130분
-- product_id 48: benefit_sms → 1,000건, benefit_voice_call → 200분
UPDATE mobile_plan
SET benefit_voice_call = '130분'
WHERE product_id = 42;

UPDATE mobile_plan
SET benefit_sms = '1,000건',
    benefit_voice_call = '200분'
WHERE product_id = 48;
