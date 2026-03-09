package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
import site.holliverse.shared.util.EncryptionTool;

// jOOQ가 생성한 테이블 객체들 (Static Import)
import static site.holliverse.admin.query.jooq.Tables.MEMBER;
import static site.holliverse.admin.query.jooq.Tables.PRODUCT;
import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION;
import static site.holliverse.admin.query.jooq.Tables.ADDRESS;
import static site.holliverse.admin.query.jooq.enums.ProductTypeEnum.MOBILE_PLAN;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.query.jooq.enums.MemberMembershipType;
import static site.holliverse.admin.query.jooq.Tables.SUPPORT_CASE;
import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD;
import static site.holliverse.admin.query.jooq.Tables.BUSINESS_KEYWORD_MAPPING_RESULT;
import static site.holliverse.admin.query.jooq.Tables.CONSULTATION_ANALYSIS;
import site.holliverse.admin.query.jooq.enums.MemberRoleType;
import org.jooq.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 관리자 회원 목록 조회를 위한 DAO.
 * - jOOQ를 사용하여 복잡한 동적 쿼리와 조인(Join)을 자바 코드로 작성.
 */
@Profile("admin")
@Repository
@RequiredArgsConstructor
public class AdminMemberDao {

    private final DSLContext dsl;           // jOOQ의 핵심 도구 (쿼리 실행기)
    private final EncryptionTool encryptionTool; // 이름/전화번호 검색 시 암호화 비교를 위해 필요

    // 조건 사전 (Map) 세팅
    private static final Map<String, Function<LocalDate, Condition>> DURATION_CONDITIONS = new LinkedHashMap<>();
    private static final Map<String, Function<LocalDate, Condition>> AGE_CONDITIONS = new LinkedHashMap<>();

    static {
        // 1. 가입 기간 조건 사전
        DURATION_CONDITIONS.put("UNDER_3_MONTHS", today -> MEMBER.JOIN_DATE.gt(today.minusMonths(3)));
        DURATION_CONDITIONS.put("MONTHS_3_TO_12", today -> MEMBER.JOIN_DATE.le(today.minusMonths(3)).and(MEMBER.JOIN_DATE.gt(today.minusYears(1))));
        DURATION_CONDITIONS.put("YEARS_1_TO_2",   today -> MEMBER.JOIN_DATE.le(today.minusYears(1)).and(MEMBER.JOIN_DATE.gt(today.minusYears(2))));
        DURATION_CONDITIONS.put("YEARS_2_TO_5",   today -> MEMBER.JOIN_DATE.le(today.minusYears(2)).and(MEMBER.JOIN_DATE.gt(today.minusYears(5))));
        DURATION_CONDITIONS.put("YEARS_5_TO_10",  today -> MEMBER.JOIN_DATE.le(today.minusYears(5)).and(MEMBER.JOIN_DATE.gt(today.minusYears(10))));
        DURATION_CONDITIONS.put("OVER_10_YEARS",  today -> MEMBER.JOIN_DATE.le(today.minusYears(10)));

        // 2. 연령대 조건 사전
        AGE_CONDITIONS.put("UNDER_10", today -> MEMBER.BIRTH_DATE.gt(today.minusYears(10)));
        AGE_CONDITIONS.put("TEENS", today -> MEMBER.BIRTH_DATE.le(today.minusYears(10)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(20))));
        AGE_CONDITIONS.put("TWENTIES", today -> MEMBER.BIRTH_DATE.le(today.minusYears(20)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(30))));
        AGE_CONDITIONS.put("THIRTIES", today -> MEMBER.BIRTH_DATE.le(today.minusYears(30)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(40))));
        AGE_CONDITIONS.put("FORTIES", today -> MEMBER.BIRTH_DATE.le(today.minusYears(40)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(50))));
        AGE_CONDITIONS.put("FIFTIES", today -> MEMBER.BIRTH_DATE.le(today.minusYears(50)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(60))));
        AGE_CONDITIONS.put("SIXTIES_EARLY", today -> MEMBER.BIRTH_DATE.le(today.minusYears(60)).and(MEMBER.BIRTH_DATE.gt(today.minusYears(65))));
        AGE_CONDITIONS.put("OVER_65", today -> MEMBER.BIRTH_DATE.le(today.minusYears(65)));
    }

    /**
     * 조건에 맞는 회원 목록을 조회하여 반환.
     * Member(회원) --(1:N)--> Subscription(구독) --(N:1)--> Product(상품)
     * -> 회원의 요금제 이름을 알기 위해 3개의 테이블을 연결해야 함.
     */
    public List<MemberRawData> findAll(AdminMemberListRequestDto req) {

        // 가장 최근에 가입한 모바일 요금제 1건만 가져오기
        Table<?> RECENT_MOBILE_PLAN = DSL.select(PRODUCT.NAME.as("planName"))
                .from(SUBSCRIPTION)
                .join(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                .where(SUBSCRIPTION.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .and(PRODUCT.PRODUCT_TYPE.eq(MOBILE_PLAN))
                .orderBy(SUBSCRIPTION.START_DATE.desc())
                .limit(1)
                .asTable("recent_mobile_plan");

        return dsl.select(
                        // 1. 회원 기본 정보 매핑
                        MEMBER.MEMBER_ID.as("id"),          // POJO의 'id' 필드에 넣기 위해 별칭 지정
                        MEMBER.NAME.as("encryptedName"),    // DB의 암호문 -> POJO 'encryptedName'
                        MEMBER.PHONE.as("encryptedPhone"),  // DB의 암호문 -> POJO 'encryptedPhone'
                        MEMBER.GENDER,
                        MEMBER.BIRTH_DATE,
                        MEMBER.EMAIL,
                        MEMBER.MEMBERSHIP,
                        MEMBER.JOIN_DATE,
                        MEMBER.STATUS,
                        // 2. 요금제 정보
                        RECENT_MOBILE_PLAN.field("planName", String.class)
                )
                .from(MEMBER) // 메인 테이블: 회원
                .leftJoin(DSL.lateral(RECENT_MOBILE_PLAN)).on(DSL.trueCondition())
                .where(createConditions(req))
                .orderBy(MEMBER.MEMBER_ID.desc())
                .limit(req.size())
                .offset(req.getOffset())
                .fetchInto(MemberRawData.class);
    }

    /**
     * 검색 조건에 맞는 '전체 데이터 개수' 조회.
     * - 용도: 프론트엔드에서 하단 페이지네이션 버튼([1][2][3]...)을 만들기 위해 필요.
     * - findAll과 똑같은 조건(Where)과 조인(Join)을 걸어야 정확한 개수가 나옴.
     */
    public long count(AdminMemberListRequestDto req) {
        Long totalCount = dsl.selectCount()
                .from(MEMBER)
                .where(createConditions(req))
                .fetchOne(0, Long.class); // 결과가 1행 1열(숫자)이므로 Long으로 변환해서 반환
        return totalCount != null ? totalCount : 0L; // null이면 0L(0)을 반환하고, 아니면 값을 반환
    }

    /**
     * 특정 회원의 상세 정보 조회 (4단 조인: MEMBER + ADDRESS + SUBSCRIPTION + PRODUCT)
     * - 반환값이 없을 수 있으므로 Optional로 감싸서 반환
     */
    public Optional<MemberDetailRawData> findDetailById(Long memberId) {

        // [최적화] LATERAL JOIN에 사용할 파생 테이블(Subquery) 미리 정의
        // 이 회원의 가장 최신 상담 내역 딱 1줄만 가져오는 쿼리
        Table<?> RECENT_SUPPORT = DSL.select(
                        SUPPORT_CASE.CREATED_AT.as("lastSupportDate"),
                        SUPPORT_CASE.STATUS.cast(String.class).as("recentSupportStatus"),
                        SUPPORT_CASE.SATISFACTION_SCORE.as("recentSatisfactionScore")
                )
                .from(SUPPORT_CASE)
                .where(SUPPORT_CASE.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .orderBy(SUPPORT_CASE.CREATED_AT.desc())
                .limit(1)
                .asTable("recent_support");

        // 가장 최근에 가입한 모바일 요금제 1건만 가져오기
        Table<?> RECENT_MOBILE_PLAN = DSL.select(
                        PRODUCT.NAME.as("currentMobilePlan"),
                        SUBSCRIPTION.START_DATE.as("contractStartDate"),
                        SUBSCRIPTION.CONTRACT_MONTHS.as("contractMonths"),
                        SUBSCRIPTION.CONTRACT_END_DATE.as("contractEndDate")
                )
                .from(SUBSCRIPTION)
                .join(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                .where(SUBSCRIPTION.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                .and(PRODUCT.PRODUCT_TYPE.eq(MOBILE_PLAN))
                .orderBy(SUBSCRIPTION.START_DATE.desc()) // 가장 최근 시작일 기준 내림차순
                .limit(1)                                // 무조건 1개만
                .asTable("recent_mobile_plan");

        return dsl.select(
                        // 1. 회원 기본 정보
                        MEMBER.NAME.as("encryptedName"),
                        MEMBER.PHONE.as("encryptedPhone"),
                        MEMBER.EMAIL,
                        MEMBER.BIRTH_DATE,
                        MEMBER.GENDER,
                        MEMBER.MEMBERSHIP,
                        MEMBER.JOIN_DATE,
                        MEMBER.STATUS,

                        // 2. 주소 정보
                        ADDRESS.PROVINCE,
                        ADDRESS.CITY,
                        ADDRESS.STREET_ADDRESS,

                        // 3. 모바일 요금제 정보
                        RECENT_MOBILE_PLAN.field("currentMobilePlan", String.class),

                        // 4. 약정 정보
                        RECENT_MOBILE_PLAN.field("contractStartDate", java.time.LocalDateTime.class),
                        RECENT_MOBILE_PLAN.field("contractMonths", Integer.class),
                        RECENT_MOBILE_PLAN.field("contractEndDate", java.time.LocalDateTime.class),

                        // 5. 상담 이력 통계
                        DSL.selectCount()
                                .from(SUPPORT_CASE)
                                .where(SUPPORT_CASE.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                                .asField("totalSupportCount"),

                        RECENT_SUPPORT.field("lastSupportDate", java.time.LocalDateTime.class),
                        RECENT_SUPPORT.field("recentSupportStatus", String.class),
                        RECENT_SUPPORT.field("recentSatisfactionScore", Integer.class),

                        // 상담 평점 (모든 만족도 점수의 평균)
                        DSL.select(DSL.avg(SUPPORT_CASE.SATISFACTION_SCORE).cast(Double.class)) // 소수점 반환
                                .from(SUPPORT_CASE)
                                .where(SUPPORT_CASE.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                                .asField("averageSatisfactionScore")
                )
                .from(MEMBER)

                // 회원 -> 주소
                .leftJoin(ADDRESS).on(MEMBER.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))

                .leftJoin(DSL.lateral(RECENT_SUPPORT)).on(DSL.trueCondition())
                .leftJoin(DSL.lateral(RECENT_MOBILE_PLAN)).on(DSL.trueCondition())

                // 검색 조건: 대상 회원의 ID
                .where(MEMBER.MEMBER_ID.eq(memberId))

                // 결과가 1건이거나 없으므로 Optional 반환
                .fetchOptionalInto(MemberDetailRawData.class);
    }

    /**
     특정 회원의 상담 분석 키워드 Top 3 추출
     */
    public List<String> findTop3KeywordsByMemberId(Long memberId) {
        return dsl.select(BUSINESS_KEYWORD.KEYWORD_NAME)
                .from(SUPPORT_CASE)
                .join(CONSULTATION_ANALYSIS).on(SUPPORT_CASE.CASE_ID.eq(CONSULTATION_ANALYSIS.CASE_ID))
                .join(BUSINESS_KEYWORD_MAPPING_RESULT).on(CONSULTATION_ANALYSIS.ANALYSIS_ID.eq(BUSINESS_KEYWORD_MAPPING_RESULT.ANALYSIS_ID))
                .join(BUSINESS_KEYWORD).on(BUSINESS_KEYWORD_MAPPING_RESULT.BUSINESS_KEYWORD_ID.eq(BUSINESS_KEYWORD.BUSINESS_KEYWORD_ID))
                .where(SUPPORT_CASE.MEMBER_ID.eq(memberId))
                .groupBy(BUSINESS_KEYWORD.KEYWORD_NAME)      // 키워드 이름으로 그룹화
                .orderBy(DSL.sum(BUSINESS_KEYWORD_MAPPING_RESULT.COUNT).desc()) // 가장 많이 나온 순서대로 정렬
                .limit(3) // 3개만 추출
                .fetchInto(String.class); // List<String> 형태로 반환
    }

    // ==========================================
    // 동적 WHERE 절 생성 로직
    // ==========================================
    private List<Condition> createConditions(AdminMemberListRequestDto req) {
        // 조건 장바구니
        List<Condition> conditions = new ArrayList<>(); // Condition = SQL의 조건식

        LocalDate today = LocalDate.now();

        // 기본 조건: 역할이 'CUSTOMER'인 회원만 조회
        conditions.add(MEMBER.ROLE.eq(MemberRoleType.CUSTOMER));

        // 1. 검색어 (암호화 일치 검색)
        if (StringUtils.hasText(req.keyword())) {
            String encryptedKeyword = encryptionTool.encrypt(req.keyword());

            conditions.add(
                    MEMBER.NAME.eq(encryptedKeyword)      // 이름이랑 같거나
                            .or(MEMBER.PHONE.eq(encryptedKeyword)) // 전화번호랑 같거나
            );
        }

        // 2. 등급 (다중 선택 가능)
        // 프론트에서 ["VIP", "GOLD"] 처럼 리스트로 보내면 -> IN ('VIP', 'GOLD') 쿼리로 변환됨
        if (!CollectionUtils.isEmpty(req.memberships())) {
            conditions.add(MEMBER.MEMBERSHIP.in(req.memberships()));
        }

        // 3. 성별 (다중 선택)
        if (!CollectionUtils.isEmpty(req.genders())) {
            conditions.add(MEMBER.GENDER.in(req.genders()));
        }

        // 4. 요금제명 (대분류 간 AND, 소분류 간 OR 필터링)
        // MEMBER 테이블에는 요금제 이름이 없어서, 위에서 조인한 PRODUCT 테이블 컬럼을 사용해야 함
        if (!CollectionUtils.isEmpty(req.planNames())) {
            // 프론트에서 넘어온 상품들이 총 몇 개의 '대분류(PRODUCT_TYPE)'인지 계산 (예: 모바일, 부가서비스 = 2)
            var targetCategoryCount = DSL.select(DSL.countDistinct(PRODUCT.PRODUCT_TYPE))
                    .from(PRODUCT)
                    .where(PRODUCT.PRODUCT_CODE.in(req.planNames()));

            // 이 회원이 가진 상품 중, 검색어와 일치하는 상품들의 '대분류' 개수 계산
            var memberMatchedCategoryCount = DSL.select(DSL.countDistinct(PRODUCT.PRODUCT_TYPE))
                    .from(SUBSCRIPTION)
                    .join(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                    .where(SUBSCRIPTION.MEMBER_ID.eq(MEMBER.MEMBER_ID))
                    .and(PRODUCT.PRODUCT_CODE.in(req.planNames()));

            // [결과] 두 개수가 완벽히 같으면 조건 만족
            conditions.add(
                    DSL.field(memberMatchedCategoryCount).eq(DSL.field(targetCategoryCount))
            );
        }

        // 5. 가입 기간 다중 필터링
        if (!CollectionUtils.isEmpty(req.durations())) {
            Condition durationCondition = req.durations().stream()
                    // 단어장에서 공식을 찾고, 오늘 날짜(today)를 넣어서 조건식을 만든다
                    .map(durationStr -> DURATION_CONDITIONS.getOrDefault(durationStr, t -> DSL.noCondition()).apply(today))
                    // 만들어진 조건식들을 전부 OR(.or())로 이어 붙인다
                    .reduce(DSL.noCondition(), Condition::or);

            conditions.add(durationCondition);
        }

        // 6. 연령대 다중 필터링
        if (!CollectionUtils.isEmpty(req.ages())) {
            Condition ageCondition = req.ages().stream()
                    .map(ageStr -> AGE_CONDITIONS.getOrDefault(ageStr, t -> DSL.noCondition()).apply(today))
                    .reduce(DSL.noCondition(), Condition::or);

            conditions.add(ageCondition);
        }

        // 7. 회원 상태 다중 선택
        if (!CollectionUtils.isEmpty(req.statuses())) {
            conditions.add(MEMBER.STATUS.in(req.statuses()));
        }

        return conditions;
    }

    // ==========================================
    // 회원 존재 여부 확인
    // ==========================================
    public boolean existsById(Long memberId) {
        return dsl.fetchExists(
                dsl.selectFrom(MEMBER)
                        .where(MEMBER.MEMBER_ID.eq(memberId))
        );
    }

    // ==========================================
    // 회원 정보 (부분) 수정
    // ==========================================
    public void updateMember(Long memberId, String encryptedName, String encryptedPhone, MemberStatusType status, MemberMembershipType membership) {

        // 1. 동적 업데이트 쿼리 객체 생성
        UpdateQuery<?> query = dsl.updateQuery(MEMBER);
        boolean hasUpdate = false; // 수정할 데이터가 하나라도 있는지 체크하는 플래그

        // 2. 값이 존재하는 필드만 SET 절에 추가 (동적 매핑)
        if (StringUtils.hasText(encryptedName)) {
            query.addValue(MEMBER.NAME, encryptedName);
            hasUpdate = true;
        }

        if (StringUtils.hasText(encryptedPhone)) {
            query.addValue(MEMBER.PHONE, encryptedPhone);
            hasUpdate = true;
        }

        // StringUtils.hasText 대신 null 체크로 변경
        if (status != null) {
            query.addValue(MEMBER.STATUS, status);
            hasUpdate = true;
        }

        // StringUtils.hasText 대신 null 체크로 변경
        if (membership != null) {
            query.addValue(MEMBER.MEMBERSHIP, membership);
            hasUpdate = true;
        }

        // 3. 만약 프론트에서 아무 값도 안 보냈다면? 쿼리 실행 없이 종료
        if (!hasUpdate) {
            return;
        }

        // 4. WHERE 조건 달고 실행
        query.addConditions(MEMBER.MEMBER_ID.eq(memberId));
        query.execute();
    }

    // ==========================================
    // 회원 상태 일괄 변경 (Bulk Update)
    // ==========================================
    /**
     * 여러 명의 회원 상태를 한 번의 쿼리로 동시에 변경(정지 등)
     * @param memberIds 변경할 대상 회원들의 ID 리스트
     * @param targetStatus 변경할 목표 상태값 (Enum)
     * @return 실제로 업데이트된 데이터의 행(Row) 개수
     */
    public int updateMembersStatus(List<Long> memberIds, MemberStatusType targetStatus) {
        // 리스트가 비어있으면 쿼리를 날리지 않고 0을 반환 (DB 부하 방지)
        if (CollectionUtils.isEmpty(memberIds)) {
            return 0;
        }

        // SQL: UPDATE member SET status = 'BANNED' WHERE member_id IN (1, 2, 3);
        return dsl.update(MEMBER)
                .set(MEMBER.STATUS, targetStatus) // 무엇을 바꿀 것인가? (상태값)
                .where(MEMBER.MEMBER_ID.in(memberIds)) // 누구를 바꿀 것인가? (ID 리스트)
                .execute(); // 실행 -> 업데이트된 Row 개수 반환
    }

    // ==========================================
    // 통합 테스트 검증용 유틸 메서드
    // ==========================================
    /**
     * 특정 ID를 가진 회원이 특정 상태(Status)인지 확인
     * 통합 테스트에서 업데이트가 잘 되었는지 검증할 때 사용
     */
    public boolean existsByIdAndStatus(Long memberId, MemberStatusType status) {
        return dsl.fetchExists(
                dsl.selectFrom(MEMBER)
                        .where(MEMBER.MEMBER_ID.eq(memberId))
                        .and(MEMBER.STATUS.eq(status))
        );
    }
}