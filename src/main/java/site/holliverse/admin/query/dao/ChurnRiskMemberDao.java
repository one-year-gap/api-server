package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.CaseValueStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import site.holliverse.admin.web.dto.churn.ChurnRiskMemberListRequestDto;
import site.holliverse.shared.util.EncryptionTool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static site.holliverse.admin.query.jooq.Tables.CHURN_SCORE_SNAPSHOT;
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;

/**
 * 이탈 위험군 목록 조회용 DAO.
 *
 * 기존 AdminMemberDao와 동일하게 jOOQ 기반 동적 조건 조립 방식을 사용한다.
 * 주요 규칙은 다음과 같다.
 * - 기준일은 "오늘" 스냅샷만 조회
 * - 기본 위험도는 HIGH / MEDIUM만 조회
 * - 회원 역할은 CUSTOMER만 허용
 * - 검색은 이름 / 전화번호 exact 검색
 *
 * 이 DAO는 "조회"만 담당한다.
 * - 복호화
 * - 이름/전화번호 마스킹
 * - risk_reasons JSON 파싱
 * - No 계산
 * 같은 화면용 가공은 Assembler로 넘긴다.
 */
@Profile("admin")
@Repository
@RequiredArgsConstructor
public class ChurnRiskMemberDao {

    //주크 쿼리 진입 시점
    private final DSLContext dsl;
    //암호화 툴
    private final EncryptionTool encryptionTool;

    /**
     * 화면에 보여줄 목록 데이터 조회.
     *
     * select 대상:
     * - 회원 기본 정보(member)
     * - 이탈 스냅샷 정보(churn_score_snapshot)
     *
     * 정렬 규칙:
     * 1. HIGH 먼저
     * 2. 같은 위험도 내에서는 churn_score 내림차순
     * 3. 마지막 tie-breaker는 member_id 내림차순
     */
    public List<ChurnRiskMemberRawData> findAll(ChurnRiskMemberListRequestDto req) {
        return dsl.select(
                        MEMBER.MEMBER_ID.as("memberId"),
                        MEMBER.MEMBERSHIP.as("membership"),
                        MEMBER.NAME.as("encryptedName"),
                        CHURN_SCORE_SNAPSHOT.RISK_LEVEL.as("riskLevel"),
                        CHURN_SCORE_SNAPSHOT.CHURN_SCORE.as("churnScore"),
                        CHURN_SCORE_SNAPSHOT.RISK_REASONS.cast(String.class).as("riskReasons"),
                        MEMBER.PHONE.as("encryptedPhone"),
                        MEMBER.EMAIL.as("email")
                )
                .from(CHURN_SCORE_SNAPSHOT)
                .join(MEMBER).on(CHURN_SCORE_SNAPSHOT.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .where(createConditions(req))
                .orderBy(riskLevelOrder(), CHURN_SCORE_SNAPSHOT.CHURN_SCORE.desc(), MEMBER.MEMBER_ID.desc())
                .limit(req.size())
                .offset(req.getOffset())
                .fetchInto(ChurnRiskMemberRawData.class);
    }

    /**
     * 페이지네이션 계산을 위한 전체 건수 조회
     */
    public long count(ChurnRiskMemberListRequestDto req) {
        Long totalCount = dsl.selectCount()
                .from(CHURN_SCORE_SNAPSHOT)
                .join(MEMBER).on(CHURN_SCORE_SNAPSHOT.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .where(createConditions(req))
                .fetchOne(0, Long.class);
        return totalCount != null ? totalCount : 0L;
    }

    /**
     * 동적 WHERE 조건 조립.
     *
     * 기본 조건:
     * - 오늘 기준 스냅샷
     * - CUSTOMER 역할만 조회
     *
     * 선택 조건:
     * - keyword: 이름/전화번호 exact 검색
     * - memberships: 등급 필터
     * - riskLevels: 위험도 필터
     *
     * 위험도 필터가 비어있으면 기본적으로 High와 MEDIUM 만 보이도록 설정
     */
    private List<Condition> createConditions(ChurnRiskMemberListRequestDto req) {
        List<Condition> conditions = new ArrayList<>();

        conditions.add(CHURN_SCORE_SNAPSHOT.BASE_DATE.eq(LocalDate.now()));
        conditions.add(MEMBER.ROLE.eq(MemberRoleType.CUSTOMER));

        if (StringUtils.hasText(req.keyword())) {
            // 기존 관리자 회원 검색과 동일하게 원문 키워드를 암호화한 뒤
            // 이름 또는 전화번호 컬럼과 exact match 비교를 수행
            String encryptedKeyword = encryptionTool.encrypt(req.keyword());
            conditions.add(
                    MEMBER.NAME.eq(encryptedKeyword)
                            .or(MEMBER.PHONE.eq(encryptedKeyword))
            );
        }

        if (!CollectionUtils.isEmpty(req.memberships())) {
            // memberships=VIP&memberships=GOLD 형태의 다중 선택 필터를 IN 조건으로 변환
            conditions.add(MEMBER.MEMBERSHIP.in(req.memberships()));
        }

        if (!CollectionUtils.isEmpty(req.riskLevels())) {
            // 사용자가 HIGH 또는 MEDIUM 중 일부만 보고 싶을 때 적용되는 사용자 지정 필터
            conditions.add(CHURN_SCORE_SNAPSHOT.RISK_LEVEL.in(req.riskLevels()));
        } else {
            // 별도 필터를 주지 않아도 LOW는 기본적으로 제외
            conditions.add(CHURN_SCORE_SNAPSHOT.RISK_LEVEL.in("HIGH", "MEDIUM"));
        }

        return conditions;
    }

    /**
     * 문자열 위험도를 화면 요구 순서대로 정렬하기 위한 가상 정렬 필드.
     *
     * 사전순 정렬을 사용하면 HIGH/MEDIUM 순서를 보장할 수 없으므로,
     * CASE 문으로 우선순위 숫자를 직접 만들어 order by에 사용
     */
    private Field<Integer> riskLevelOrder() {
        CaseValueStep<String> riskLevelCase = org.jooq.impl.DSL.case_(CHURN_SCORE_SNAPSHOT.RISK_LEVEL);
        return riskLevelCase
                .when("HIGH", 1)
                .when("MEDIUM", 2)
                .otherwise(3);
    }
}
