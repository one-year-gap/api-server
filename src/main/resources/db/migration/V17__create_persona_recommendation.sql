-- 페르소나 기반 추천 캐시 (회원당 1행, 7일 캐시는 updated_at 기준)
CREATE TABLE persona_recommendation (
    member_id                BIGINT           NOT NULL PRIMARY KEY,
    segment                  VARCHAR(20)      NOT NULL
        CHECK (segment IN ('upsell', 'churn_risk', 'normal')),
    cached_llm_recommendation TEXT            NOT NULL,
    recommended_products     JSONB            NOT NULL DEFAULT '[]',
    updated_at               TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_persona_recommendation_member
        FOREIGN KEY (member_id) REFERENCES member (member_id)
);
