-- Bulk seed for local performance / analytics testing
-- Target:
--  - member: 50,000 rows
--  - support_case: 200,000 rows
-- Notes:
--  - Re-runnable. Existing bulk-seed rows are cleaned up first.
--  - question_text is generated as ~500 chars and always includes 상담 키워드.

BEGIN;

-- 0) Cleanup previous 200k bulk seed rows (re-runnable)
DELETE FROM support_case sc
USING member m
WHERE sc.member_id = m.member_id
  AND m.email LIKE 'seed.bulk.member.%@test.local'
  AND sc.title LIKE '[SEED-BULK-200K] %';

DELETE FROM member
WHERE email LIKE 'seed.bulk.member.%@test.local';

DELETE FROM address
WHERE street_address LIKE 'seed-bulk-street-%';

-- 1) Prepare deterministic seed source for 50,000 members
CREATE TEMP TABLE tmp_seed_member (
    idx BIGINT PRIMARY KEY,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    street_address VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(1) NOT NULL,
    join_date DATE NOT NULL,
    membership member_membership_type NOT NULL,
    children_count INTEGER NOT NULL,
    address_id BIGINT
) ON COMMIT DROP;

INSERT INTO tmp_seed_member (
    idx, province, city, street_address, postal_code,
    email, phone, birth_date, gender, join_date, membership, children_count
)
SELECT
    gs,
    CASE (gs % 10)
        WHEN 0 THEN '서울특별시'
        WHEN 1 THEN '경기도'
        WHEN 2 THEN '인천광역시'
        WHEN 3 THEN '부산광역시'
        WHEN 4 THEN '대구광역시'
        WHEN 5 THEN '광주광역시'
        WHEN 6 THEN '대전광역시'
        WHEN 7 THEN '울산광역시'
        WHEN 8 THEN '세종특별자치시'
        ELSE '강원특별자치도'
    END AS province,
    CASE (gs % 10)
        WHEN 0 THEN '강남구'
        WHEN 1 THEN '성남시'
        WHEN 2 THEN '연수구'
        WHEN 3 THEN '해운대구'
        WHEN 4 THEN '수성구'
        WHEN 5 THEN '서구'
        WHEN 6 THEN '유성구'
        WHEN 7 THEN '남구'
        WHEN 8 THEN '한솔동'
        ELSE '춘천시'
    END AS city,
    'seed-bulk-street-' || gs AS street_address,
    lpad(((10000 + gs) % 100000)::text, 5, '0') AS postal_code,
    format('seed.bulk.member.%s@test.local', lpad(gs::text, 6, '0')) AS email,
    '099' || lpad(gs::text, 8, '0') AS phone,
    date '1970-01-01' + ((gs * 13) % 12000) AS birth_date,
    CASE WHEN gs % 2 = 0 THEN 'M' ELSE 'F' END AS gender,
    current_date - ((gs * 7) % 3650) AS join_date,
    CASE (gs % 4)
        WHEN 0 THEN 'BASIC'::member_membership_type
        WHEN 1 THEN 'GOLD'::member_membership_type
        WHEN 2 THEN 'VIP'::member_membership_type
        ELSE 'VVIP'::member_membership_type
    END AS membership,
    (gs % 4)::int AS children_count
FROM generate_series(1, 50000) AS gs;

-- 2) Insert address rows and map address_id back to temp table
INSERT INTO address (province, city, street_address, postal_code)
SELECT province, city, street_address, postal_code
FROM tmp_seed_member;

UPDATE tmp_seed_member t
SET address_id = a.address_id
FROM address a
WHERE a.province = t.province
  AND a.city = t.city
  AND a.street_address = t.street_address;

-- 3) Insert 50,000 member rows
INSERT INTO member (
    address_id,
    provider_id,
    email,
    password,
    name,
    phone,
    birth_date,
    gender,
    join_date,
    status,
    type,
    role,
    membership,
    children_count
)
SELECT
    address_id,
    NULL,
    email,
    'seed-bulk-password',
    format('seed-member-%s', lpad(idx::text, 6, '0')),
    phone,
    birth_date,
    gender,
    join_date,
    'ACTIVE'::member_status_type,
    'FORM'::member_signup_type,
    'CUSTOMER'::member_role_type,
    membership,
    children_count
FROM tmp_seed_member
WHERE address_id IS NOT NULL;

-- 4) Insert 200,000 support_case rows
--    question_text: 500 chars + 상담 키워드(kw1, kw2, kw3) 포함
WITH member_pool AS (
    SELECT array_agg(member_id) AS member_ids
    FROM member
    WHERE email LIKE 'seed.bulk.member.%@test.local'
),
category_pool AS (
    SELECT array_agg(category_code) AS category_codes
    FROM category
),
keyword_pool AS (
    SELECT ARRAY[
        '모바일', '휴대폰', '요금제', '요금 납부', '인터넷', 'TV',
        '멤버십', '해외로밍', '통화품질', '결합상품', '가입', '해지',
        '앱오류', '로그인', '와이파이 설정', '장애 점검', '국제전화',
        '개인정보침해신고', '자동이체 신청', '위약금 조회', '유심 변경',
        '번호이동 문의', '기기 변경', '멤버십 등급', '채널추가'
    ]::text[] AS kws
),
generated AS (
    SELECT
        gs,
        mp.member_ids[(1 + floor(random() * array_length(mp.member_ids, 1)))::int] AS member_id,
        cp.category_codes[(1 + floor(random() * array_length(cp.category_codes, 1)))::int] AS category_code,
        kp.kws[(1 + floor(random() * array_length(kp.kws, 1)))::int] AS kw1,
        kp.kws[(1 + floor(random() * array_length(kp.kws, 1)))::int] AS kw2,
        kp.kws[(1 + floor(random() * array_length(kp.kws, 1)))::int] AS kw3,
        random() AS r_status,
        now()
            - make_interval(days => (random() * 365)::int)
            - make_interval(secs => (random() * 86400)::int) AS created_at_base
    FROM generate_series(1, 200000) AS gs
    CROSS JOIN member_pool mp
    CROSS JOIN category_pool cp
    CROSS JOIN keyword_pool kp
),
prepared AS (
    SELECT
        gs,
        member_id,
        category_code,
        kw1,
        kw2,
        kw3,
        CASE
            WHEN r_status < 0.50 THEN 'OPEN'::support_status
            WHEN r_status < 0.80 THEN 'SUPPORTING'::support_status
            ELSE 'CLOSED'::support_status
        END AS status,
        created_at_base AS created_at,
        created_at_base + make_interval(mins => (random() * 120)::int) AS customer_modified_at,
        (5 + (random() * 180)::int) AS start_offset_mins,
        (10 + (random() * 480)::int) AS resolve_offset_mins,
        left(
            format(
                '고객 상담 문의입니다. %s / %s / %s 관련 확인 부탁드립니다. ',
                kw1, kw2, kw3
            ) || repeat(md5(random()::text), 25),
            500
        ) AS question_text,
        left(
            format('%s 문의에 대해 확인 후 안내드립니다. ', kw1)
            || repeat(md5(random()::text), 12),
            320
        ) AS answer_seed,
        (1 + floor(random() * 5))::int AS satisfaction
    FROM generated
)
INSERT INTO support_case (
    member_id,
    counselor_id,
    category_code,
    status,
    title,
    question_text,
    answer_text,
    satisfaction_score,
    customer_modified_at,
    support_started_at,
    resolved_at,
    created_at,
    updated_at
)
SELECT
    member_id,
    NULL,
    category_code,
    status,
    format('[SEED-BULK-200K] 상담 문의 #%s (%s)', lpad(gs::text, 6, '0'), kw1),
    question_text,
    CASE WHEN status = 'OPEN'::support_status THEN NULL ELSE answer_seed END AS answer_text,
    CASE WHEN status = 'CLOSED'::support_status THEN satisfaction ELSE NULL END AS satisfaction_score,
    customer_modified_at,
    CASE
        WHEN status = 'OPEN'::support_status THEN NULL
        ELSE created_at + make_interval(mins => start_offset_mins)
    END AS support_started_at,
    CASE
        WHEN status = 'CLOSED'::support_status THEN created_at + make_interval(mins => (start_offset_mins + resolve_offset_mins))
        ELSE NULL
    END AS resolved_at,
    created_at,
    now()
FROM prepared;

COMMIT;

-- Optional quick checks
-- SELECT count(*) FROM member WHERE email LIKE 'seed.bulk.member.%@test.local';
-- SELECT count(*) FROM support_case WHERE title LIKE '[SEED-BULK-200K] %';
