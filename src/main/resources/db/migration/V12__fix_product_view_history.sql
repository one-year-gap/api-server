-- ============================================================
-- product_view_history 테이블 재정의
-- V1 구조(view_id PK, FK) -> 복합 PK + 비정규화 컬럼, FK 없음
-- ============================================================

-- STEP 1: 새 구조의 임시 테이블 생성
CREATE TABLE product_view_history_new (
    member_id     BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(100) NOT NULL,
    product_type  VARCHAR(50)  NOT NULL,
    tags          JSONB,
    viewed_at     TIMESTAMPTZ  NOT NULL,
    last_event_id BIGINT       NOT NULL,

    CONSTRAINT pk_member_product PRIMARY KEY (member_id, product_id)
);

-- STEP 2: 기존 데이터 이전 (member_id, product_id 당 최신 1건)
-- 조인 없이 이관: 과거 메타는 기본값으로 채우고, 이후 로그 유입 시 실제 값 반영
INSERT INTO product_view_history_new (member_id, product_id, product_name, product_type, tags, viewed_at, last_event_id)
SELECT DISTINCT ON (pvh.member_id, pvh.product_id)
    pvh.member_id,
    pvh.product_id,
    'UNKNOWN',
    'UNKNOWN',
    NULL::JSONB,
    pvh.viewed_at::timestamptz,
    pvh.view_id
FROM product_view_history pvh
ORDER BY pvh.member_id, pvh.product_id, pvh.viewed_at DESC;

-- STEP 3: 기존 테이블 삭제 후 이름 변경
DROP TABLE product_view_history;
ALTER TABLE product_view_history_new RENAME TO product_view_history;

-- STEP 4: RAG/최근 본 상품 조회용 복합 인덱스
CREATE INDEX idx_pvh_member_viewed ON product_view_history (member_id, viewed_at DESC);
