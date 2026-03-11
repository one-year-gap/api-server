CREATE TABLE member_llm_context (
    -- 기본키 / 식별자
    member_id BIGINT NOT NULL,
    
    -- 기본 프로필
    membership      VARCHAR(20)  NOT NULL,         -- GOLD / VIP / VVIP 등
    age_group       VARCHAR(10)  NOT NULL,         -- "10대" / "20대" / ...
    join_months     INTEGER      NOT NULL,         -- 가입 개월 수
    children_count  INTEGER      NOT NULL DEFAULT 0,
    
    -- 가족 결합
    family_group_num INTEGER     NOT NULL DEFAULT 0,  -- 같은 family_group_id 인원 수
    family_role      VARCHAR(30),                     -- REPRESENTATIVE / MEMBER 등
    
    -- 페르소나 (코드만 저장, FK 없음)
    persona_code    VARCHAR(50), -- SPACE_EXPLORER, SPACE_GUARDIAN, SPACE_SURFER, SPACE_OCTOPUS, SPACE_GRAVITY, SPACE_SHERLOCK
    
    -- 세그먼트 (1차 룰 베이스 결과)
    segment VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
        CHECK (segment IN ('CHURN_RISK', 'UPSELL', 'NORMAL')),
    
    -- 구독 및 사용량
    current_subscriptions   JSONB   NOT NULL DEFAULT '[]'::jsonb,
    -- 예: [{ "product_id": 1, "product_type": "MOBILE_PLAN", "product_name": "5G 에센셜", "price": 10000,"sale_price": 8000 }, ...]

    
    current_product_types   JSONB   NOT NULL DEFAULT '{}'::jsonb,
    -- 예: {"MOBILE_PLAN": true, "INTERNET": true}
    
    current_data_usage_ratio INTEGER,   -- (사용량/제공량)*100, 예: 110.2
    data_usage_pattern       VARCHAR(10)
        CHECK (data_usage_pattern IN ('OVER', 'FIT', 'UNDER') OR data_usage_pattern IS NULL),
    -- 무제한 요금제는 NULL 허용
    
    -- 이탈 지표
    churn_score INTEGER,   -- 0.00 ~ 100.00
    
    churn_tier  VARCHAR(20)
        CHECK (churn_tier IN ('HIGH', 'MEDIUM', 'LOW') OR churn_tier IS NULL),
    
    -- 최근 상담 요약 (최대 3개 제목 합쳐서 저장)
    recent_counseling TEXT,
    
    -- 행동/관심 태그
    recent_viewed_tags_top_3 JSONB NOT NULL DEFAULT '[]'::jsonb,
    -- 예: ["데이터무제한", "OTT프리미엄", "가족결합메인"]
    
    -- 약정 정보
    contract_expiry_within_3m BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 메타
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT pk_member_llm_context PRIMARY KEY (member_id),
    CONSTRAINT fk_member_llm_context_member
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON DELETE CASCADE
);

-- 추천 쿼리 성능 향상을 위한 인덱스 추가
CREATE INDEX idx_member_llm_context_segment ON member_llm_context (segment);
CREATE INDEX idx_member_llm_context_updated_at ON member_llm_context (updated_at);

-- recommended_products JSONB 확장 형식 문서화 (항목: rank, productId, productName, productType, productPrice, salePrice, tags, llmReason)
COMMENT ON COLUMN persona_recommendation.recommended_products IS
'JSON 배열. 각 항목: rank(INT), productId(BIGINT), productName(VARCHAR), productType(VARCHAR), productPrice(INT), salePrice(INT), tags(JSON 배열), llmReason(TEXT)';