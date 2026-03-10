-- 10,000건의 더미 상담 데이터를 현재 스키마 기준으로 적재한다.
-- 기존 member/category 데이터를 재사용하며, keyword 분석 테이블은 건드리지 않는다.
--
-- 실행 예시
-- psql -h 127.0.0.1 -U postgres -d holliverse -f scripts/seed-support-case-dummy-10000.sql
--
-- 정리 예시
-- DELETE FROM support_case
-- WHERE title LIKE '[DUMMY-SUPPORT-20260310] %';

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM member
        WHERE status = 'ACTIVE'
          AND role = 'CUSTOMER'
    ) THEN
        RAISE EXCEPTION 'ACTIVE CUSTOMER member가 없습니다.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM category
    ) THEN
        RAISE EXCEPTION 'category 마스터 데이터가 없습니다.';
    END IF;
END $$;

WITH customer_pool AS (
    SELECT row_number() OVER (ORDER BY member_id) AS rn,
           member_id
    FROM member
    WHERE status = 'ACTIVE'
      AND role = 'CUSTOMER'
),
operator_base AS (
    SELECT DISTINCT member_id
    FROM (
        SELECT member_id
        FROM member
        WHERE status = 'ACTIVE'
          AND role IN ('COUNSELOR', 'ADMIN')

        UNION ALL

        SELECT member_id
        FROM member
        WHERE status = 'ACTIVE'
          AND NOT EXISTS (
              SELECT 1
              FROM member
              WHERE status = 'ACTIVE'
                AND role IN ('COUNSELOR', 'ADMIN')
          )
    ) src
),
operator_pool AS (
    SELECT row_number() OVER (ORDER BY member_id) AS rn,
           member_id
    FROM operator_base
),
category_pool AS (
    SELECT row_number() OVER (ORDER BY category_code) AS rn,
           category_code,
           category_group_code,
           category_name
    FROM category
),
pool_size AS (
    SELECT (SELECT count(*) FROM customer_pool) AS customer_count,
           (SELECT count(*) FROM operator_pool) AS operator_count,
           (SELECT count(*) FROM category_pool) AS category_count
),
seed AS (
    SELECT gs AS seq,
           rnd.customer_pick,
           rnd.operator_pick,
           rnd.category_pick,
           rnd.status_pick,
           rnd.created_pick,
           rnd.customer_edit_pick,
           rnd.start_pick,
           rnd.resolve_pick,
           rnd.score_pick
    FROM generate_series(1, 10000) AS gs
    CROSS JOIN LATERAL (
        SELECT random() AS customer_pick,
               random() AS operator_pick,
               random() AS category_pick,
               random() AS status_pick,
               random() AS created_pick,
               random() AS customer_edit_pick,
               random() AS start_pick,
               random() AS resolve_pick,
               random() AS score_pick
    ) rnd
),
picked AS (
    SELECT s.seq,
           c.member_id,
           CASE
               WHEN ps.operator_count = 0 THEN NULL
               WHEN op.member_id = c.member_id AND ps.operator_count > 1 THEN op_next.member_id
               ELSE op.member_id
           END AS counselor_id,
           cat.category_code,
           cat.category_group_code,
           cat.category_name,
           CASE
               WHEN s.status_pick < 0.18 THEN 'OPEN'::support_status
               WHEN s.status_pick < 0.42 THEN 'SUPPORTING'::support_status
               ELSE 'CLOSED'::support_status
           END AS status,
           date_trunc('minute', now() - interval '365 days' + (s.created_pick * interval '365 days')) AS created_at,
           s.customer_edit_pick,
           s.start_pick,
           s.resolve_pick,
           s.score_pick
    FROM seed s
    CROSS JOIN pool_size ps
    JOIN customer_pool c
      ON c.rn = (1 + floor(s.customer_pick * ps.customer_count))::int
    JOIN category_pool cat
      ON cat.rn = (1 + floor(s.category_pick * ps.category_count))::int
    LEFT JOIN operator_pool op
      ON ps.operator_count > 0
     AND op.rn = (1 + floor(s.operator_pick * ps.operator_count))::int
    LEFT JOIN operator_pool op_next
      ON ps.operator_count > 1
     AND op_next.rn = CASE
         WHEN (1 + floor(s.operator_pick * ps.operator_count))::int >= ps.operator_count THEN 1
         ELSE (1 + floor(s.operator_pick * ps.operator_count))::int + 1
     END
),
timed AS (
    SELECT p.seq,
           p.member_id,
           p.counselor_id,
           p.category_code,
           p.category_group_code,
           p.category_name,
           p.status,
           p.created_at,
           CASE
               WHEN p.customer_edit_pick < 0.30
               THEN p.created_at + ((5 + floor(p.customer_edit_pick * 20))::int * interval '1 minute')
               ELSE NULL
           END AS customer_modified_at,
           CASE
               WHEN p.status = 'OPEN'::support_status THEN NULL
               ELSE p.created_at + ((30 + floor(p.start_pick * 1410))::int * interval '1 minute')
           END AS support_started_at,
           p.resolve_pick,
           p.score_pick
    FROM picked p
),
final_rows AS (
    SELECT t.member_id,
           CASE
               WHEN t.status = 'OPEN'::support_status THEN NULL
               ELSE t.counselor_id
           END AS counselor_id,
           t.category_code,
           t.status,
           format('[DUMMY-SUPPORT-20260310] %s 문의 %s', t.category_name, lpad(t.seq::text, 5, '0')) AS title,
           CASE t.category_group_code
               WHEN 'CG_MOB' THEN format('모바일 %s 관련 문의입니다. 요금/혜택 적용 시점과 처리 절차를 확인하고 싶습니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               WHEN 'CG_HOM' THEN format('홈상품 %s 관련 문의입니다. 설치 일정과 이용 상태를 확인 부탁드립니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               WHEN 'CG_INT' THEN format('국제전화/부가전화 %s 관련 문의입니다. 사용 가능 여부와 요금 기준을 알고 싶습니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               WHEN 'CG_WEB' THEN format('홈페이지 %s 관련 문의입니다. 로그인 또는 계정 처리 방법을 안내받고 싶습니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               WHEN 'CG_MEM' THEN format('멤버십 %s 관련 문의입니다. 혜택 적용 조건과 사용 방법을 확인 부탁드립니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               WHEN 'CG_PRV' THEN format('개인정보 %s 관련 문의입니다. 본인 확인과 접수 절차를 안내받고 싶습니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
               ELSE format('%s 관련 문의입니다. 처리 절차를 확인 부탁드립니다. 접수번호 %s', t.category_name, lpad(t.seq::text, 5, '0'))
           END AS question_text,
           CASE
               WHEN t.status = 'OPEN'::support_status THEN NULL
               WHEN t.status = 'SUPPORTING'::support_status THEN
                   CASE t.category_group_code
                       WHEN 'CG_MOB' THEN format('모바일 %s 건은 현재 확인 중입니다. 사용 중인 회선과 요금 정보를 검토한 뒤 안내드리겠습니다.', t.category_name)
                       WHEN 'CG_HOM' THEN format('홈상품 %s 건은 접수 완료되었으며 설치/품질 이력을 확인 중입니다. 확인 후 순차 안내드리겠습니다.', t.category_name)
                       WHEN 'CG_INT' THEN format('국제전화/부가전화 %s 건은 사용 이력과 과금 기준을 검토 중입니다. 확인 후 답변드리겠습니다.', t.category_name)
                       WHEN 'CG_WEB' THEN format('홈페이지 %s 건은 계정 상태를 점검 중입니다. 점검 후 필요한 조치 방법을 안내드리겠습니다.', t.category_name)
                       WHEN 'CG_MEM' THEN format('멤버십 %s 건은 혜택 대상 여부를 확인 중입니다. 확인되는 대로 안내드리겠습니다.', t.category_name)
                       WHEN 'CG_PRV' THEN format('개인정보 %s 건은 본인 확인 절차와 접수 기준을 검토 중입니다. 확인 후 안내드리겠습니다.', t.category_name)
                       ELSE format('%s 건은 현재 확인 중입니다. 검토 후 안내드리겠습니다.', t.category_name)
                   END
               ELSE
                   CASE t.category_group_code
                       WHEN 'CG_MOB' THEN format('모바일 %s 문의는 처리 완료되었습니다. 적용 가능 조건과 반영 시점을 기준으로 안내드렸습니다.', t.category_name)
                       WHEN 'CG_HOM' THEN format('홈상품 %s 문의는 처리 완료되었습니다. 설치/변경 가능 일정과 유의사항을 기준으로 안내드렸습니다.', t.category_name)
                       WHEN 'CG_INT' THEN format('국제전화/부가전화 %s 문의는 처리 완료되었습니다. 이용 가능 여부와 요금 기준을 정리해 안내드렸습니다.', t.category_name)
                       WHEN 'CG_WEB' THEN format('홈페이지 %s 문의는 처리 완료되었습니다. 계정 상태 확인 후 필요한 조치 방법을 안내드렸습니다.', t.category_name)
                       WHEN 'CG_MEM' THEN format('멤버십 %s 문의는 처리 완료되었습니다. 혜택 조건과 사용 방법을 기준으로 안내드렸습니다.', t.category_name)
                       WHEN 'CG_PRV' THEN format('개인정보 %s 문의는 처리 완료되었습니다. 본인 확인 절차와 후속 조치를 안내드렸습니다.', t.category_name)
                       ELSE format('%s 문의는 처리 완료되었습니다. 확인 결과를 기준으로 안내드렸습니다.', t.category_name)
                   END
           END AS answer_text,
           CASE
               WHEN t.status = 'CLOSED'::support_status THEN (1 + floor(t.score_pick * 5))::int
               ELSE NULL
           END AS satisfaction_score,
           t.customer_modified_at,
           t.support_started_at,
           CASE
               WHEN t.status = 'CLOSED'::support_status
               THEN t.support_started_at + ((10 + floor(t.resolve_pick * 2870))::int * interval '1 minute')
               ELSE NULL
           END AS resolved_at,
           t.created_at,
           CASE
               WHEN t.status = 'CLOSED'::support_status
               THEN t.support_started_at + ((10 + floor(t.resolve_pick * 2870))::int * interval '1 minute')
               WHEN t.status = 'SUPPORTING'::support_status
               THEN t.support_started_at
               ELSE COALESCE(t.customer_modified_at, t.created_at)
           END AS updated_at
    FROM timed t
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
SELECT member_id,
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
FROM final_rows;

COMMIT;
