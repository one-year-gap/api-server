-- ==========================================
-- 1. 5G/LTE 요금제 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_001', '5G 프리미어 에센셜', 85000, 58500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 무제한',
    '테더링+쉐어링 70GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_002', '5G 스탠다드', 75000, 56250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 150GB',
    '기본제공량 내 테더링+쉐어링 60GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_003', '5G 프리미어 레귤러', 95000, 66000, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 무제한',
    '테더링+쉐어링 80GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    '밀리의 서재 | 아이들나라(스탠다드+러닝) | 지니뮤직(genie) | 구글 원(Google One)',
    '콘텐츠, 음악 감상 등
최대 11,900원 혜택',
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_004', '유쓰 5G 스탠다드', 75000, 56250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 210GB',
    '기본제공량 내 테더링+쉐어링 65GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_005', '5G 데이터 레귤러', 63000, 47250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 50GB',
    '기본제공량 내 테더링+쉐어링 40GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_006', '5G 데이터 플러스', 66000, 49500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 80GB',
    '기본제공량 내 테더링+쉐어링 45GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_007', '5G 심플+', 61000, 45750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 31GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_008', '유쓰 5G 데이터 플러스', 66000, 49500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 110GB',
    '기본제공량 내 테더링+쉐어링 50GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_009', '5G 라이트+', 55000, 41250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 14GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_010', '유쓰 5G 라이트+', 55000, 41250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 26GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_011', '5G 미니', 37000, 27750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 5GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_012', '5G 슬림+', 47000, 35250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 9GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_013', '5G 프리미어 플러스', 105000, 73500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 무제한',
    '테더링+쉐어링 100GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    '넷플릭스 | 유튜브 프리미엄 | 디즈니+ | 티빙 | 멀티팩',
    '콘텐츠, 음악 감상 등
최대 11,900원 혜택',
    'OTT, 구독 등
최대 월 23,900원 혜택',
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_014', '5G 프리미어 슈퍼', 115000, 81000, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 무제한',
    '테더링+쉐어링 100GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    '넷플릭스 | 유튜브 프리미엄 | 디즈니+ | 티빙 | 멀티팩',
    '콘텐츠, 음악 감상 등
최대 11,900원 혜택',
    'OTT, 구독 등
최대 월 31,800원 혜택',
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_015', '5G 시니어 B형', 43000, 32250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 10GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 400분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_016', '유쓰 5G 슬림+', 47000, 35250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 15GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_017', '(LTE) 데이터 시니어 33', 33000, 24750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 1.7GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(부가통화 110분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_018', '유쓰 5G 데이터 레귤러', 63000, 47250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 70GB',
    '기본제공량 내 테더링+쉐어링 45GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_019', '5G 베이직+', 59000, 44250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 24GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_020', '5G 시니어 A형', 45000, 33750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 10GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 400분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_021', '유쓰 5G 미니', 37000, 27750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 9GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_022', '유쓰 5G 베이직+', 59000, 44250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 36GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_023', '5G 스탠다드 에센셜', 70000, 52500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 125GB',
    '기본제공량 내 테더링+쉐어링 55GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_024', '(LTE) 데이터 33', 33000, 24750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 1.5GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 110분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_025', '유쓰 5G 심플+', 61000, 45750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 41GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_026', '5G 시니어 C형', 39000, 29250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 10GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 400분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_027', '5G 데이터 슈퍼', 68000, 51000, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 95GB',
    '기본제공량 내 테더링+쉐어링 50GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_028', 'LTE 표준', 11990, 8992, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 1KB당 0.275원',
    NULL,
    '50건',
    '1초당 1.98원',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_029', '유쓰 5G 스탠다드 에센셜', 70000, 52500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 185GB',
    '기본제공량 내 테더링+쉐어링 60GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_030', '5G 시그니처', 130000, 92250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 무제한',
    '테더링+쉐어링 120GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    '넷플릭스 | 유튜브 프리미엄 | 디즈니+ | 티빙 | 멀티팩',
    '콘텐츠, 음악 감상 등
최대 11,900원 혜택',
    'OTT, 구독 등
최대 월 31,800원 혜택',
    '33,000원
(대상 요금제 한함)'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_031', '시니어16.5', 16500, 12375, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 300MB',
    NULL,
    '100건',
    '70분
(+지정번호 3개 음성통화 50분/ 영상통화 30분 사용 가능)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_032', '유쓰 5G 데이터 슈퍼', 68000, 51000, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 135GB',
    '기본제공량 내 테더링+쉐어링 55GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_033', '(LTE) 추가 요금 걱정 없는 데이터 69', 69000, 51750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 일 5GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_034', '(LTE) 현역병사 데이터 55', 55000, 41250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 매일 5GB',
    '5GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_035', '(LTE) 현역병사 데이터 33', 33000, 24750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 월2GB+매일2GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 110분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_036', '5G 라이트 청소년', 45000, 33750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 8GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_037', '(LTE) 추가 요금 걱정 없는 데이터 시니어 69', 69000, 51750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 매일 5GB',
    '테더링+쉐어링15GB',
    '기본제공',
    '집/이동전화 무제한
(부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_038', '(LTE) 추가 요금 걱정 없는 데이터 청소년 33', 33000, 24750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 2GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 110분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_039', '(LTE) 추가 요금 걱정 없는 데이터 청소년 69', 69000, 51750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 매일 5GB',
    '테더링 15GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_040', '(LTE) 복지 33', 33000, 24750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 2GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(부가통화 600분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_041', '5G 복지 55', 55000, 41250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 14GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 600분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_042', 'LTE청소년19', 20900, 15675, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 350MB',
    NULL,
    '1,000건',
    '20,000링
(1초에 2.5링)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_043', '5G 복지 75', 75000, 56250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 150GB',
    '기본제공량 내 테더링+쉐어링 60GB',
    '기본제공',
    '집/이동전화 무제한
(+부가통화 600분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_044', '(LTE) 복지 49', 49000, 36750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 6GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(부가통화 600분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_045', '5G 키즈 29', 29000, 21750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 3.3GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+ 부가통화 100분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_046', '5G 키즈 39', 39000, 29250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 5.5GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_047', '5G 키즈 45', 45000, 33750, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 9GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_048', 'LTE 선택형 요금제', 20900, 15675, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 250MB~1GB',
    NULL,
    '0건~1,000건(무료 통화형은 기본제공)',
    '100분~200분',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_049', 'LTE 키즈 22(만 12세 이하)', 22000, 16500, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 700MB',
    NULL,
    '기본제공',
    '60분
(+지정번호 2개(망내) 음성통화 무제한)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_MOB_050', '(LTE) 추가 요금 걱정 없는 데이터 청소년 59', 59000, 44250, 'MOBILE_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO mobile_plan (
    product_id, data_amount, tethering_sharing_data,
    benefit_sms, benefit_voice_call, benefit_brands,
    benefit_media, benefit_premium, benefit_signature_family_discount
)
SELECT
    product_id,
    '데이터 9GB',
    NULL,
    '기본제공',
    '집/이동전화 무제한
(+부가통화 300분)',
    NULL,
    NULL,
    NULL,
    NULL
FROM new_p;

-- ==========================================
-- 2. 태블릿/스마트워치 요금제 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_001', '5G 태블릿 4GB+데이터 나눠쓰기', 22000, 16500, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 4GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_002', 'LTE Wearable', 11000, 8250, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 250MB',
    '50분', -- 없는 경우 NULL 처리됨
    '250건'        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_003', '태블릿/스마트기기 500MB + 데이터 나눠쓰기', 11000, 8250, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 500MB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_004', '태블릿/스마트기기 데이터 10GB', 16500, 12375, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 10GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_005', '태블릿/스마트기기 데이터 20GB', 24750, 18563, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 20GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_006', '5G 태블릿 6GB+데이터 나눠쓰기', 33000, 24750, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 6GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_007', '태블릿/스마트기기 데이터 걱정없는 25GB', 65890, 49418, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 월 25GB + 일 2GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_008', '태블릿/스마트기기 3GB + 데이터 나눠쓰기', 16500, 12375, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 3GB',
    NULL, -- 없는 경우 NULL 처리됨
    NULL        -- 없는 경우 NULL 처리됨
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_TAB_009', 'LTE Wearable KIDS', 8800, 6600, 'TAB_WATCH_PLAN', '약정 할인')
    RETURNING product_id
)
INSERT INTO tab_watch_plan (
    product_id,
    data_amount,
    benefit_voice_call,
    benefit_sms
)
SELECT
    product_id,
    '데이터 200MB',
    '50분', -- 없는 경우 NULL 처리됨
    '기본제공'        -- 없는 경우 NULL 처리됨
FROM new_p;

-- ==========================================
-- 3. IPTV 요금제 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_001', '실속형', 15400, 14630, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    '217개의 채널 시청이 가능한', -- 없으면 p_name 사용: '실속형'
    217, -- 정수형(Integer)으로 들어감
    '아이들나라'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_002', '기본형', 16500, 15730, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    '223개의 채널 시청이 가능한', -- 없으면 p_name 사용: '기본형'
    223, -- 정수형(Integer)으로 들어감
    '아이들나라'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_003', '프리미엄', 18700, 17600, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    '매월 업데이트되는 최신 영화를 무료로', -- 없으면 p_name 사용: '프리미엄'
    252, -- 정수형(Integer)으로 들어감
    '프리미엄 클럽 +Pack | 아이들나라'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_004', '프리미엄 플러스', 22000, 20900, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    'UHD팩까지 포함된 모든 채널 시청', -- 없으면 p_name 사용: '프리미엄 플러스'
    257, -- 정수형(Integer)으로 들어감
    'UHD팩 | 프리미엄 클럽 +Pack | 아이들나라'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_005', '프리미엄 환승구독', 29700, 28050, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    '8개 방송사 콘텐츠를 자유롭게 즐기는', -- 없으면 p_name 사용: '프리미엄 환승구독'
    257, -- 정수형(Integer)으로 들어감
    '자유이용권 | UHD팩 | 프리미엄 클럽 +Pack | 아이들나라'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_IPTV_006', '프리미엄 유플레이', 26400, 24750, 'IPTV', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO iptv (
    product_id, plan_title, channel, benefits
)
SELECT
    product_id,
    '유플레이 모든 콘텐츠를 편하게 시청', -- 없으면 p_name 사용: '프리미엄 유플레이'
    257, -- 정수형(Integer)으로 들어감
    '유플레이 | UHD팩 | 프리미엄 클럽 +Pack | 아이들나라'
FROM new_p;

-- ==========================================
-- 4. 인터넷 요금제 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_001', '와이파이기본_기가안심 1G', 38500, 36300, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '고사양 게임을 즐길 수 있는', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '1Gbps',
    '1Gbps 속도 | 기가와이파이6 기본 제공 | 유해사이트, 악성코드 차단 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_002', '와이파이기본_기가슬림안심 500M', 33000, 31350, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '고화질 동영상 콘텐츠도 끊김없이', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '500Mbps',
    '500Mbps 속도 | 기가와이파이6 기본 제공 | 유해사이트, 악성코드 차단 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_003', '와이파이기본_광랜안심 100M', 22000, 20600, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '인터넷 검색, 문서 작업을 편리하게', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '100Mbps',
    '100Mbps 속도 | 기가와이파이6 기본 제공 | 유해사이트, 악성코드 차단 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_004', '프리미엄 안심 보상 1G', 45100, 42900, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    'PC 수리비 보상까지, 완벽한 인터넷', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '1Gbps',
    '대용량 영상을 끊김 없이 시청할 수 있는 속도 | 기가와이파이6 또는 기가와이파이7 중 1개 선택 | 내 라이프 스타일에 맞춰 원하는 기기 1대 추가 선택 | PC 추가 연결 서비스 | 유해/스미싱 의심 사이트 차단 | 금융 피해/수리비 보상 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_005', '프리미엄 안심 보상 500M', 39600, 37950, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '보안+금융 보상까지, 스마트한 선택', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '500Mbps',
    '대용량 영상을 끊김 없이 시청할 수 있는 속도 | 기가와이파이6 또는 기가와이파이7 중 1개 선택 | 내 라이프 스타일에 맞춰 원하는 기기 1대 추가 선택 | 유해/스미싱 의심 사이트 차단 | 금융 피해 보상 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_006', '프리미엄 안심 보상 100M', 27500, 26100, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '기본 보안까지 제공되는 합리적 인터넷', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '100Mbps',
    '웹서핑, 문서 작업을 편리하게 할 수 있는 속도 | 기가와이파이 1대 기본 제공 | 유해/스미싱 의심 사이트 차단'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_007', '프리미엄 안심 1기가', 42900, 40700, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '3대 동시 접속, 멀티 환경에 최적!', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '1Gbps',
    '대용량 영상을 끊김없이 시청할 수 있는 속도 | 기가와이파이6 1대 기본 제공 | 내 라이프 스타일에 맞춰 원하는 기기 1대 추가 선택 | 유해 사이트/악성코드 차단 | 원격으로 PC와 노트북 문제 해결 | PC와 노트북 최대 3대 동시 접속'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_008', '프리미엄 안심 500M', 37400, 35750, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '차단부터 PC원격진단까지!', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '500Mbps',
    '대용량 영상을 끊김없이 시청할 수 있는 속도 | 기가와이파이6 1대 기본 제공 | 내 라이프 스타일에 맞춰 원하는 기기 1대 추가 선택 | 유해 사이트/악성코드 차단 | 원격으로 PC와 노트북 문제 해결'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('PLAN_INT_009', '프리미엄 안심 100M', 25300, 23900, 'INTERNET', '온라인 단독 할인')
    RETURNING product_id
)
INSERT INTO internet (
    product_id,
    plan_title,
    speed,
    benefits
)
SELECT
    product_id,
    '안심하고 즐기는 인터넷!', -- CSV에 plan_title 컬럼이 있어야 함 (없으면 p_name 쓰게 수정 가능)
    '100Mbps',
    '웹서핑, 문서 작업을 편리하게 할 수 있는 속도 | 기가와이파이/스마트홈 스피커 기본 제공 | 유해 사이트/악성코드 차단'
FROM new_p;

-- ==========================================
-- 5. 부가서비스 (디지털 콘텐츠) 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_001', '구글 원(Google One)', 19800, 19800, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '구독 한 번으로 구글 AI와 구글 클라우드 저장 공간을 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_002', '유튜브 프리미엄', 14900, 14900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'U⁺통신료와 한번에 결제하고 더욱 편리하게 이용할 수 있는 유튜브 프리미엄 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_003', '티빙 프리미엄', 17000, 17000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '티빙 오리지널 콘텐츠와 tvN, JTBC, Mnet 등의 최신 방송 프로그램, 스포츠, Apple TV+ 콘텐츠, 공연, 중계, 애니메이션 등
다채로운 콘텐츠를 빠르고 편리하게 즐길 수 있는 OTT 서비스.
티빙과 함께 재미를 무제한 스트리밍하세요!'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_004', '티빙 스탠다드', 13500, 13500, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '티빙 오리지널 콘텐츠와 tvN, JTBC, Mnet 등의 최신 방송 프로그램, 스포츠, 공연 중계, 애니메이션 등
다채로운 콘텐츠를 빠르고 편리하게 즐길 수 있는 OTT 서비스.
티빙과 함께 재미를 무제한 스트리밍하세요!'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_005', '티빙 베이직', 9500, 9500, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '티빙 오리지널 콘텐츠와 tvN, JTBC, Mnet 등의 최신 방송 프로그램, 스포츠, 공연 중계, 애니메이션 등
다채로운 콘텐츠를 빠르고 편리하게 즐길 수 있는 OTT 서비스.
티빙과 함께 재미를 무제한 스트리밍하세요!'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_006', '디즈니+ 프리미엄', 13900, 13900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '디즈니+는 디즈니, 마블, 픽사, 스타워즈, 내셔널지오그래픽 및 각 지역별 오리지널 콘텐츠까지 폭 넓은 라인업의 영화, 시리즈 등의 콘텐츠를 시청할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_007', '디즈니+ 스탠다드', 9900, 9900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '디즈니+는 디즈니, 마블, 픽사, 스타워즈, 내셔널지오그래픽 및 각 지역별 오리지널 콘텐츠까지 폭 넓은 라인업의 영화, 시리즈 등의 콘텐츠를 시청할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_008', '넷플릭스 프리미엄', 17000, 17000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'U⁺통신 요금과 한번에 결제하여, 신용카드 정보를 등록할 필요없이 편리하게 넷플릭스를 이용할 수 있습니다.'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_009', '넷플릭스 스탠다드', 13500, 13500, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'U⁺통신 요금과 한번에 결제하여, 신용카드 정보를 등록할 필요없이 편리하게 넷플릭스를 이용할 수 있습니다.'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_010', '모두의할인팩', 11000, 11000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'OTT부터 최신영화 방송 VOD, 월정액까지 다양한 미디어상품을 할인 받을 수 있는 멤버십'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_011', '아이들나라 프리미엄', 19800, 19800, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '250만 부모가 선택한 대한민국 대표 키즈 콘텐츠 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_012', '아이들나라 스탠다드', 11000, 11000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '250만 부모가 선택한 대한민국 대표 키즈 콘텐츠 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_013', '아이들나라 플레이', 9900, 9900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '250만 부모가 선택한 대한민국 대표 키즈 콘텐츠 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_014', '지니뮤직', 7900, 7900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'Play Your Color : 나만을 위한 음악 큐레이션 서비스를 지니뮤직에서 만나보세요'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_015', '지니뮤직 마음껏 듣기(모바일전용)', 7900, 7900, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '지니뮤직 앱에서 데이터 무료로 음악을 감상할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_016', '지니뮤직 마음껏 듣기(모든 디바이스)', 9600, 9600, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '모든 디바이스에서 지니뮤직 음악을 감상하고, 앱에서는 데이터까지 무료로 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_017', 'V컬러링 음악감상 플러스', 8800, 8800, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'V 컬러링과 지니뮤직 앱 무제한 음악감상을 할인된 금액으로 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_018', 'g 포인트 혜택 업그레이드 A', 2200, 2200, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '요금제 혜택으로 ‘지니뮤직 앱 음악감상’을 이용하는 고객이 기기 제한 없이 음악감상을 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_019', 'g 포인트 혜택 업그레이드 B', 4400, 4400, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '요금제 혜택으로 ‘지니뮤직 300회 음악감상’을 이용하는 고객이 횟수 제한 없이 음악감상을 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_020', 'U+모바일tv', 2200, 2200, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    'U⁺오리지널 콘텐츠, 실시간 채널, 해외 드라마, 영화, 등 25만여 편의 동영상 중 내 취향에 맞는 영상을 추천 받아 마음껏 볼 수 있는 앱 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_DIG_021', '뮤직벨링(통화연결음,벨소리,MP3,지니뮤직 음악감상)', 8800, 8800, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'DIGITAL', -- ENUM 타입 강제 지정
    '뮤직벨링 묶음 부가서비스를 통해, 벨소리/통화연결음/MP3는 물론 지니뮤직 음악감상을 이용할 수 있어요.'
FROM new_p;

-- ==========================================
-- 6. 부가서비스 (가족 혜택) 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_001', '딥페이크안심보호', 4400, 4400, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '딥페이크 콘텐츠 모니터링, AI로 위조나 변조된 문서 분석 기능을 이용하고, 딥페이크 피해 보상 보험 혜택도 받을 수 있는 AI 범죄 보호 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_002', '자녀안전지킴이', 3300, 3300, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '위치 정보를 기반으로 한 위험 지역 알림, 긴급 호출, 유해 메시지 모니터링 기능으로 자녀의 안전을 관리하는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_003', '스팸차단후후', 2200, 2200, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '각종 스팸 번호를 식별해 실시간으로 차단하고, 보이스/메신저 피싱, 해킹 피해 예방과 보상까지 책임지는 통합 안심 케어 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_004', '가족안부전화', 4950, 4950, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    'AI 보호사가 매일 안부 전화를 걸어 가족의 일상과 건강을 세심하게 살피고, 위급 상황이 발생하면 보호자에게 바로 연락해 가족의 안전을 지켜주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_005', '휴대폰가족보호', 3300, 3300, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '보호가 필요한 가족 상태를 확인하고 위급 상황이 생기면 바로 대처할 수 있는 안심 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_006', '자녀폰지킴이', 2200, 2200, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '스마트폰 유해정보로부터 아이를 보호하고, 올바른 스마트폰 사용 습관을 길러 주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_007', '스마트안티피싱', 1650, 1650, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '보이스피싱 의심 데이터를 원천적으로 확보하여 피싱을 사전에 탐지하고 알려주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_008', '휴대폰번호 보호서비스', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '휴대폰번호 도용을 철저하게 방지하여 한 단계 높은 보안성을 보장하는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_009', '스마트피싱보호', 1650, 1650, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    'SNS, 뉴스 등 웹사이트에 저장된 정보와 국내 금융 및 제휴기관이 보유한 사기 관련 정보를 분석하여
보이스 및 메신저 피싱 피해를 방지하는 앱 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_010', '오토콜', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '주차 차량에 개인번호 대신 오토콜 대표번호를 남겨 개인정보 유출을 방지할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_011', '원키퍼', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '보안 앱을 설치해 휴대폰에 저장된 개인정보를 더 안전하게 관리해 주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_012', '금융사고안심팩', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '소중한 개인정보가 명의도용 및 금융사기에 이용되지 않도록 LG유플러스와 NICE평가정보가 함께 제공하는 개인정보 통합 보호 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_013', '로그인플러스', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '모바일기기와 PC로 인터넷 사이트 로그인 시 다른 사람이 내 아이디로 로그인하는 것을 차단해주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_014', '전화번호안심로그인', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '휴대폰 유심(가입자 정보 저장) 카드에 나의 계정 정보를 저장하여 각종 도용 및 해킹 문제를 예방할 수 있는
간편하고 안전한 2채널 로그인 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_015', '간편결제매니저', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '보다 안전하고 편리하게 간편결제를 진행할 수 있도록 도와주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_016', '모션키', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '아이디와 비밀번호를 입력하는 대신 휴대폰을 흔들거나 화면을 미는 등의 동작만으로 인터넷 사이트에 간편하게 로그인할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_017', '유비키인증서', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '인증서를 휴대폰에 안전하게 저장하고 필요할 때 마다 꺼내 쓸 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_018', '모바일안전결제(ISP)', 550, 550, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '모바일 안전결제(ISP) 인증서로 안전하게 신용카드 결제를 할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_019', 'USIM 스마트인증(라온시큐어)', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '인증서를 휴대폰에 저장하여 해킹이나 유출 걱정 없이 안전하게 이용할 수 있는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_020', '스마트공동인증(드림시큐리티)', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '인증서 유출을 보다 강력하게 방지해주고 편리함은 더해진 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_021', '스마트 안전결제', 990, 990, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '스미싱이 의심되는 전화나 문자메시지를 알려주고, 금융 사기 피해가 발생하면 최대 400만원까지 보상해주는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_FAM_022', 'MyOTP', 1100, 1100, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'FAMILY_CARE', -- ENUM 타입
    '개인정보 유출, 해킹, 피싱 걱정 없는 일회용 비밀번호 서비스'
FROM new_p;

-- ==========================================
-- 7. 부가서비스 (휴대폰 케어) 데이터 적재
-- ==========================================

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_PH_001', '폰교체 패스 (휴대폰보험)', 3000, 3000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'PHONE_CARE', -- ENUM 타입
    '휴대폰이 파손됐을 때 수리 또는 교체 중 원하는 방식을 선택할 수 있는 폰케어 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_PH_002', '폰 안심패스 (휴대폰보험)', 3500, 3500, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'PHONE_CARE', -- ENUM 타입
    '휴대폰 구입 후 발생되는 분실, 도난, 파손, 침수 시 단말기 교체 또는 수리비를 지원해 드리는 서비스'
FROM new_p;

WITH new_p AS (
    INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
    VALUES ('ADDON_PH_003', '폰교체 슬림 (구.맘대로 폰교체 / 맘대로 폰교체 플러스)', 5000, 5000, 'ADDON', NULL)
    RETURNING product_id
)
INSERT INTO addon_service (
    product_id, addon_type, description
)
SELECT
    product_id,
    'PHONE_CARE', -- ENUM 타입
    '언제 어디서나 원하는 휴대폰으로 교체할 수 있는 신개념 폰케어 서비스'
FROM new_p;

-- ==========================================
-- [필수] ID 시퀀스 동기화 (가장 마지막에 실행)
-- ==========================================
SELECT setval(pg_get_serial_sequence('product', 'product_id'), (SELECT MAX(product_id) FROM product));
