-- Dummy data for AdminRegionalMetricDao integration checks
-- Expected result for yyyymm = '202601':
-- - SEOUL avgSales = 20000, avgDataUsageGb = 15
-- - BUSAN avgSales = 50000, avgDataUsageGb = 0

INSERT INTO address (province, city, street_address, postal_code)
VALUES
    ('SEOUL', 'GANGNAM', 'TEST-ROAD-101', '06123'),
    ('BUSAN', 'HAEUNDAE', 'TEST-ROAD-202', '48100')
ON CONFLICT (province, city, street_address) DO NOTHING;

INSERT INTO member (
    address_id, provider_id, email, password, name, phone, birth_date, gender,
    status, type, role, membership
)
SELECT a.address_id, NULL, 'armd-seoul-1@holliverse.local', 'password123', 'SeoulCustomer1', '01070000001',
       DATE '1990-01-01', 'M', 'ACTIVE', 'FORM', 'CUSTOMER', 'VIP'
FROM address a
WHERE a.province = 'SEOUL' AND a.city = 'GANGNAM' AND a.street_address = 'TEST-ROAD-101'
ON CONFLICT (email) DO NOTHING;

INSERT INTO member (
    address_id, provider_id, email, password, name, phone, birth_date, gender,
    status, type, role, membership
)
SELECT a.address_id, NULL, 'armd-seoul-2@holliverse.local', 'password123', 'SeoulCustomer2', '01070000002',
       DATE '1992-02-02', 'F', 'ACTIVE', 'FORM', 'CUSTOMER', 'VIP'
FROM address a
WHERE a.province = 'SEOUL' AND a.city = 'GANGNAM' AND a.street_address = 'TEST-ROAD-101'
ON CONFLICT (email) DO NOTHING;

INSERT INTO member (
    address_id, provider_id, email, password, name, phone, birth_date, gender,
    status, type, role, membership
)
SELECT a.address_id, NULL, 'armd-busan-1@holliverse.local', 'password123', 'BusanCustomer1', '01070000003',
       DATE '1994-03-03', 'M', 'ACTIVE', 'FORM', 'CUSTOMER', 'GOLD'
FROM address a
WHERE a.province = 'BUSAN' AND a.city = 'HAEUNDAE' AND a.street_address = 'TEST-ROAD-202'
ON CONFLICT (email) DO NOTHING;

INSERT INTO product (product_code, name, price, sale_price, product_type, discount_type)
VALUES
    ('ARMD_SEOUL_P1', 'SEOUL_TEST_PLAN_1', 10000, 10000, 'MOBILE_PLAN', 'NONE'),
    ('ARMD_SEOUL_P2', 'SEOUL_TEST_PLAN_2', 30000, 30000, 'MOBILE_PLAN', 'NONE'),
    ('ARMD_BUSAN_P1', 'BUSAN_TEST_PLAN_1', 50000, 50000, 'MOBILE_PLAN', 'NONE')
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO subscription (member_id, product_id, status)
SELECT m.member_id, p.product_id, TRUE
FROM member m
JOIN product p ON p.product_code = 'ARMD_SEOUL_P1'
WHERE m.email = 'armd-seoul-1@holliverse.local'
  AND NOT EXISTS (
      SELECT 1
      FROM subscription s
      WHERE s.member_id = m.member_id
        AND s.product_id = p.product_id
  );

INSERT INTO subscription (member_id, product_id, status)
SELECT m.member_id, p.product_id, TRUE
FROM member m
JOIN product p ON p.product_code = 'ARMD_SEOUL_P2'
WHERE m.email = 'armd-seoul-2@holliverse.local'
  AND NOT EXISTS (
      SELECT 1
      FROM subscription s
      WHERE s.member_id = m.member_id
        AND s.product_id = p.product_id
  );

INSERT INTO subscription (member_id, product_id, status)
SELECT m.member_id, p.product_id, TRUE
FROM member m
JOIN product p ON p.product_code = 'ARMD_BUSAN_P1'
WHERE m.email = 'armd-busan-1@holliverse.local'
  AND NOT EXISTS (
      SELECT 1
      FROM subscription s
      WHERE s.member_id = m.member_id
        AND s.product_id = p.product_id
  );

INSERT INTO usage_monthly (subscription_id, yyyymm, usage_details)
SELECT s.subscription_id, '202601', '{"data_gb": 10}'::jsonb
FROM subscription s
JOIN member m ON m.member_id = s.member_id
JOIN product p ON p.product_id = s.product_id
WHERE m.email = 'armd-seoul-1@holliverse.local'
  AND p.product_code = 'ARMD_SEOUL_P1'
  AND NOT EXISTS (
      SELECT 1
      FROM usage_monthly u
      WHERE u.subscription_id = s.subscription_id
        AND u.yyyymm = '202601'
  );

INSERT INTO usage_monthly (subscription_id, yyyymm, usage_details)
SELECT s.subscription_id, '202601', '{"data_gb": 20}'::jsonb
FROM subscription s
JOIN member m ON m.member_id = s.member_id
JOIN product p ON p.product_id = s.product_id
WHERE m.email = 'armd-seoul-2@holliverse.local'
  AND p.product_code = 'ARMD_SEOUL_P2'
  AND NOT EXISTS (
      SELECT 1
      FROM usage_monthly u
      WHERE u.subscription_id = s.subscription_id
        AND u.yyyymm = '202601'
  );

-- Noise data for another month (must be excluded when querying 202601)
INSERT INTO usage_monthly (subscription_id, yyyymm, usage_details)
SELECT s.subscription_id, '202512', '{"data_gb": 999}'::jsonb
FROM subscription s
JOIN member m ON m.member_id = s.member_id
JOIN product p ON p.product_id = s.product_id
WHERE m.email = 'armd-seoul-1@holliverse.local'
  AND p.product_code = 'ARMD_SEOUL_P1'
  AND NOT EXISTS (
      SELECT 1
      FROM usage_monthly u
      WHERE u.subscription_id = s.subscription_id
        AND u.yyyymm = '202512'
  );
