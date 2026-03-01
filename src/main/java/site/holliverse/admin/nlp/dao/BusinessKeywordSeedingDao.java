package site.holliverse.admin.nlp.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD;
import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD_ALIAS;

@Repository
@RequiredArgsConstructor
public class BusinessKeywordSeedingDao {

    private final DSLContext dsl;

    // 1. 데이터 존재 여부 확인 (시딩 중복 방지)
    public boolean hasData() {
        return dsl.fetchExists(dsl.selectFrom(BUSINESS_KEYWORD));
    }

    // 2. 비즈니스 키워드 마스터 저장 후 생성된 ID 반환
    public Long saveKeywordAndGetId(String keywordCode, String keywordName) {
        return dsl.insertInto(BUSINESS_KEYWORD)
                .set(BUSINESS_KEYWORD.KEYWORD_CODE, keywordCode)
                .set(BUSINESS_KEYWORD.KEYWORD_NAME, keywordName)
                .set(BUSINESS_KEYWORD.IS_ACTIVE, true) // 시딩 데이터는 기본 활성화
                .returningResult(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID) // 생성된 PK 값 가져오기
                .fetchOneInto(Long.class);
    }

    // 3. 비즈니스 키워드 별칭 저장
    public void saveAlias(Long businessKeywordId, String aliasText, String aliasNorm) {
        dsl.insertInto(BUSINESS_KEYWORD_ALIAS)
                .set(BUSINESS_KEYWORD_ALIAS.BUSINESS_KEYWORD_ID, businessKeywordId)
                .set(BUSINESS_KEYWORD_ALIAS.ALIAS_TEXT, aliasText)
                .set(BUSINESS_KEYWORD_ALIAS.ALIAS_NORM, aliasNorm)
                .set(BUSINESS_KEYWORD_ALIAS.IS_ACTIVE, true)
                .execute();
    }
}