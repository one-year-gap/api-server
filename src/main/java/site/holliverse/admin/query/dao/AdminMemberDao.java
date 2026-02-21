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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * 조건에 맞는 회원 목록을 조회하여 반환.
     * Member(회원) --(1:N)--> Subscription(구독) --(N:1)--> Product(상품)
     * -> 회원의 요금제 이름을 알기 위해 3개의 테이블을 연결해야 함.
     */
    public List<MemberRawData> findAll(AdminMemberListRequestDto req) {
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
                        // 2. 요금제 정보 (Product 테이블에서 가져옴)
                        // 구독이 없으면 NULL이 들어갈 수 있음
                        PRODUCT.NAME.as("planName")
                )
                .from(MEMBER) // 메인 테이블: 회원

                // [조인 1] 회원 -> 구독 (현재 구독 중인 것만 연결)
                // LEFT JOIN 이유: 요금제 없는(구독 안 한) 회원도 목록에는 나와야 하니까!
                // (INNER JOIN을 쓰면 구독 안 한 회원은 목록에서 아예 사라짐)
                .leftJoin(SUBSCRIPTION).on(
                        MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID)
                                .and(SUBSCRIPTION.STATUS.isTrue()) // status = TRUE (해지 안 한 구독만)
                )

                // [조인 2] 구독 -> 상품 (요금제 이름을 가져오기 위해 연결)
                .leftJoin(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))

                .where(createConditions(req)) // 아래 만들어둔 동적 필터 조건 적용
                .orderBy(MEMBER.MEMBER_ID.desc())    // 최신 가입자가 먼저 나오도록 내림차순 정렬
                .limit(req.size())            // 몇 개 가져올지 (예: 10개)
                .offset(req.getOffset())      // 앞에서 몇 개 건너뛸지
                .fetchInto(MemberRawData.class); // 결과를 POJO 객체 리스트로 변환
    }

    /**
     * 검색 조건에 맞는 '전체 데이터 개수' 조회.
     * - 용도: 프론트엔드에서 하단 페이지네이션 버튼([1][2][3]...)을 만들기 위해 필요.
     * - findAll과 똑같은 조건(Where)과 조인(Join)을 걸어야 정확한 개수가 나옴.
     */
    public long count(AdminMemberListRequestDto req) {
        Long totalCount = dsl.selectCount()
                .from(MEMBER)
                .leftJoin(SUBSCRIPTION).on(
                        MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID)
                                .and(SUBSCRIPTION.STATUS.isTrue())
                )
                .leftJoin(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                .where(createConditions(req))
                .fetchOne(0, Long.class); // 결과가 1행 1열(숫자)이므로 Long으로 변환해서 반환
        return totalCount != null ? totalCount : 0L; // null이면 0L(0)을 반환하고, 아니면 값을 반환
    }

    /**
     * 특정 회원의 상세 정보 조회 (4단 조인: MEMBER + ADDRESS + SUBSCRIPTION + PRODUCT)
     * - 반환값이 없을 수 있으므로 Optional로 감싸서 반환
     */
    public Optional<MemberDetailRawData> findDetailById(Long memberId) {
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
                        PRODUCT.NAME.as("currentMobilePlan")
                )
                .from(MEMBER)

                // [조인 1] 회원 -> 주소
                .leftJoin(ADDRESS).on(MEMBER.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))

                // [조인 2] 회원 -> 구독 (현재 활성화된 구독만)
                .leftJoin(SUBSCRIPTION).on(
                        MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID)
                                .and(SUBSCRIPTION.STATUS.isTrue())
                )

                // [조인 3] 구독 -> 상품 (MOBILE_PLAN 만)
                .leftJoin(PRODUCT).on(
                        SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID)
                                .and(PRODUCT.PRODUCT_TYPE.eq(MOBILE_PLAN))
                )

                // 검색 조건: 대상 회원의 ID
                .where(MEMBER.MEMBER_ID.eq(memberId))

                // 결과가 1건이거나 없으므로 Optional 반환
                .fetchOptionalInto(MemberDetailRawData.class);
    }

    // ==========================================
    // 동적 WHERE 절 생성 로직
    // ==========================================
    private List<Condition> createConditions(AdminMemberListRequestDto req) {
        // 조건 장바구니
        List<Condition> conditions = new ArrayList<>(); // Condition = SQL의 조건식

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

        // 3. 성별 (단일 선택)
        if (StringUtils.hasText(req.gender())) {
            conditions.add(MEMBER.GENDER.eq(req.gender()));
        }

        // 4. 요금제명 (다중 선택 가능)
        // MEMBER 테이블에는 요금제 이름이 없어서, 위에서 조인한 PRODUCT 테이블 컬럼을 사용해야 함
        if (!CollectionUtils.isEmpty(req.planNames())) {
            conditions.add(PRODUCT.NAME.in(req.planNames()));
        }

        // 5. 가입일 범위 검색 (기간 조회)
        if (req.joinDateStart() != null) {
            // 가입일이 시작일보다 크거나 같음 (>=)
            conditions.add(MEMBER.JOIN_DATE.ge(req.joinDateStart()));
        }

        if (req.joinDateEnd() != null) {
            // 가입일이 종료일보다 작거나 같음 (<=)
            conditions.add(MEMBER.JOIN_DATE.le(req.joinDateEnd()));
        }

        // 6. 연령대 다중 필터링
        // 요청: [20, 40] (20대 또는 40대인 사람을 찾아줘)
        // 논리: (20대 조건) OR (40대 조건)
        if (!CollectionUtils.isEmpty(req.ageGroups())) {
            Condition ageCondition = DSL.noCondition(); // 빈 조건으로 시작해서 OR로 붙여나감
            LocalDate today = LocalDate.now();

            for (Integer ageStart : req.ageGroups()) {
                // 예: ageStart = 20 (20대 검색)
                // 만 나이 계산법 역산:
                // - 가장 늦게 태어난 사람(제일 어린 20세): 오늘 날짜 - 20년
                // - 가장 일찍 태어난 사람(제일 많은 29세): 오늘 날짜 - 30년 + 1일

                LocalDate maxBirth = today.minusYears(ageStart);
                LocalDate minBirth = today.minusYears(ageStart + 10).plusDays(1);

                // OR 조건으로 계속 연결: (20대 범위) OR (40대 범위) OR ...
                ageCondition = ageCondition.or(MEMBER.BIRTH_DATE.between(minBirth, maxBirth));
            }

            // 완성된 연령대 조건 덩어리를 전체 검색 조건에 추가 (괄호로 감싸짐)
            conditions.add(ageCondition);
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
}