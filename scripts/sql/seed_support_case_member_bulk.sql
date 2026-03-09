-- Bulk seed for local performance testing
-- Target:
--  - member: 10,000 rows
--  - support_case: 30,000 rows
-- Notes:
--  - Re-runnable. Existing bulk-seed rows are cleaned up first.
--  - question_text is generated as ~500 random characters.

BEGIN;

-- 0) Cleanup previous bulk seed rows (re-runnable)
DELETE FROM support_case sc
USING member m
WHERE sc.member_id = m.member_id
  AND m.email LIKE 'seed.bulk.member.%@test.local'
  AND sc.title LIKE '[SEED-BULK] %';

DELETE FROM member
WHERE email LIKE 'seed.bulk.member.%@test.local';

DELETE FROM address
WHERE street_address LIKE 'seed-bulk-street-%';

-- 1) Prepare deterministic seed source for 10,000 members
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
    CASE (gs % 5)
        WHEN 0 THEN '서울특별시'
        WHEN 1 THEN '경기도'
        WHEN 2 THEN '인천광역시'
        WHEN 3 THEN '부산광역시'
        ELSE '대구광역시'
    END AS province,
    CASE (gs % 5)
        WHEN 0 THEN '강남구'
        WHEN 1 THEN '성남시'
        WHEN 2 THEN '연수구'
        WHEN 3 THEN '해운대구'
        ELSE '수성구'
    END AS city,
    'seed-bulk-street-' || gs AS street_address,
    lpad(((10000 + gs) % 100000)::text, 5, '0') AS postal_code,
    format('seed.bulk.member.%s@test.local', lpad(gs::text, 5, '0')) AS email,
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
FROM generate_series(1, 10000) AS gs;

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

-- 3) Insert 10,000 member rows
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
    format('seed-member-%s', lpad(idx::text, 5, '0')),
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

-- 4) Insert 30,000 support_case rows with ~500-char random question_text
WITH member_pool AS (
    SELECT array_agg(member_id) AS member_ids
    FROM member
    WHERE email LIKE 'seed.bulk.member.%@test.local'
),
category_pool AS (
    SELECT array_agg(category_code) AS category_codes
    FROM category
),
generated AS (
    SELECT
        gs,
        mp.member_ids[(1 + floor(random() * array_length(mp.member_ids, 1)))::int] AS member_id,
        cp.category_codes[(1 + floor(random() * array_length(cp.category_codes, 1)))::int] AS category_code,
        random() AS r_status,
        now()
            - make_interval(days => (random() * 180)::int)
            - make_interval(secs => (random() * 86400)::int) AS created_at_base
    FROM generate_series(1, 30000) AS gs
    CROSS JOIN member_pool mp
    CROSS JOIN category_pool cp
),
prepared AS (
    SELECT
        gs,
        member_id,
        category_code,
        CASE
            WHEN r_status < 0.55 THEN 'OPEN'::support_status
            WHEN r_status < 0.85 THEN 'SUPPORTING'::support_status
            ELSE 'CLOSED'::support_status
        END AS status,
        created_at_base AS created_at,
        created_at_base + make_interval(mins => (random() * 120)::int) AS customer_modified_at,
        left(repeat(md5(random()::text), 16), 500) AS question_text,
        left(repeat(md5(random()::text), 10), 300) AS answer_seed,
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
    format('[SEED-BULK] 상담 문의 #%s', lpad(gs::text, 5, '0')),
    question_text,
    CASE WHEN status = 'OPEN'::support_status THEN NULL ELSE answer_seed END AS answer_text,
    CASE WHEN status = 'CLOSED'::support_status THEN satisfaction ELSE NULL END AS satisfaction_score,
    customer_modified_at,
    CASE
        WHEN status = 'OPEN'::support_status THEN NULL
        ELSE created_at + make_interval(mins => (5 + (random() * 180)::int))
    END AS support_started_at,
    CASE
        WHEN status = 'CLOSED'::support_status THEN created_at + make_interval(mins => (200 + (random() * 600)::int))
        ELSE NULL
    END AS resolved_at,
    created_at,
    now()
FROM prepared;

COMMIT;
