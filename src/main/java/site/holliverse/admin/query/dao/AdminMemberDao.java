//package site.holliverse.admin.query.dao;
//
//import lombok.RequiredArgsConstructor;
//import org.jooq.Condition;
//import org.jooq.DSLContext;
//import org.jooq.impl.DSL;
//import org.springframework.stereotype.Repository;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//import site.holliverse.admin.web.dto.member.AdminMemberListRequestDto;
//import site.holliverse.shared.util.EncryptionTool;
//
//// jOOQ가 생성한 테이블 객체들 (Static Import)
//// *주의: 실제 jOOQ 생성 파일명에 따라 대소문자가 다를 수 있습니다. (보통 대문자)
//import static site.holliverse.admin.query.jooq.Tables.MEMBER;
//import static site.holliverse.admin.query.jooq.Tables.PRODUCT;      // 상품 테이블
//import static site.holliverse.admin.query.jooq.Tables.SUBSCRIPTION; // 구독 테이블
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 관리자 회원 목록 조회를 위한 DAO.
// * jOOQ를 사용하여 동적 쿼리와 복잡한 조인을 처리합니다.
// */
//@Repository
//@RequiredArgsConstructor
//public class AdminMemberDao {
//
//    private final DSLContext dsl;
//    private final EncryptionTool encryptionTool;
//
//    /**
//     * 필터 조건에 맞는 회원 목록을 조회합니다.
//     * 관계: Member(1) -> Subscription(N) -> Product(1)
//     */
//    public List<MemberRawData> findAll(AdminMemberListRequestDto req) {
//        return dsl.select(
//                        // 1. 회원 기본 정보
//                        MEMBER.MEMBER_ID.as("id"),          // POJO의 id 필드와 매핑하기 위해 별칭(alias) 사용
//                        MEMBER.NAME.as("encryptedName"),    // 암호화된 이름
//                        MEMBER.PHONE.as("encryptedPhone"),  // 암호화된 전화번호
//                        MEMBER.GENDER,
//                        MEMBER.BIRTH_DATE,
//                        MEMBER.EMAIL,
//                        MEMBER.MEMBERSHIP.as("grade"),      // DB 컬럼은 membership, DTO는 grade
//                        MEMBER.JOIN_DATE,
//                        MEMBER.STATUS,
//
//                        // 2. 요금제 정보 (상품명)
//                        // 구독이 없거나 해지된 경우 NULL이 나올 수 있음
//                        PRODUCT.NAME.as("planName")
//                )
//                .from(MEMBER)
//
//                // [조인 1] 회원 -> 구독 (현재 구독 중인 것만)
//                // LEFT JOIN을 쓰는 이유: 요금제가 없는 회원도 목록에는 나와야 하니까!
//                .leftJoin(SUBSCRIPTION).on(
//                        MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID)
//                                .and(SUBSCRIPTION.STATUS.isTrue()) // status = TRUE (현재 구독중)인 것만 조인
//                )
//
//                // [조인 2] 구독 -> 상품 (요금제 이름 가져오기 위해)
//                .leftJoin(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
//
//                .where(createConditions(req)) // ★ 아래에 정의한 동적 WHERE 절 호출
//                .orderBy(MEMBER.MEMBER_ID.desc())    // 최신 가입순 정렬
//                .limit(req.size())            // 페이지 사이즈 (예: 10개)
//                .offset(req.getOffset())      // 건너뛸 개수 (예: 1페이지면 0, 2페이지면 10)
//                .fetchInto(MemberRawData.class); // 결과를 POJO 클래스에 자동으로 담기
//    }
//
//    /**
//     * 전체 데이터 개수 조회 (페이징 계산용).
//     * 조건에 맞는 회원이 총 몇 명인지 세어야 프론트에서 [1][2][3] 페이지 버튼을 만들 수 있음.
//     */
//    public long count(AdminMemberListRequestDto req) {
//        return dsl.selectCount()
//                .from(MEMBER)
//                // 개수 셀 때도 검색 조건(요금제 이름 등)이 걸릴 수 있으므로 똑같이 조인해야 함
//                .leftJoin(SUBSCRIPTION).on(
//                        MEMBER.MEMBER_ID.eq(SUBSCRIPTION.MEMBER_ID)
//                                .and(SUBSCRIPTION.STATUS.isTrue())
//                )
//                .leftJoin(PRODUCT).on(SUBSCRIPTION.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
//                .where(createConditions(req))
//                .fetchOne(0, Long.class); // 첫 번째 컬럼을 Long 타입으로 반환
//    }
//
//    // ==========================================
//    // 동적 WHERE 절 생성 로직 (핵심)
//    // ==========================================
//    private List<Condition> createConditions(AdminMemberListRequestDto req) {
//        List<Condition> conditions = new ArrayList<>();
//
//        // 1. 검색어 (암호화 일치 검색)
//        // 사용자가 입력한 평문(예: "이수빈")을 암호화해서 DB의 암호문과 비교
//        if (StringUtils.hasText(req.keyword())) {
//            String encryptedKeyword = encryptionTool.encrypt(req.keyword());
//
//            conditions.add(
//                    MEMBER.NAME.eq(encryptedKeyword)      // 이름이 같거나
//                            .or(MEMBER.PHONE.eq(encryptedKeyword)) // 전화번호가 같거나
//            );
//        }
//
//        // 2. 등급 (다중 선택) -> DB 컬럼명: MEMBERSHIP
//        // List로 들어오면 자동으로 IN (...) 쿼리로 변환됨
//        if (!CollectionUtils.isEmpty(req.grades())) {
//            conditions.add(MEMBER.MEMBERSHIP.in(req.grades()));
//        }
//
//        // 3. 성별 (단일 선택)
//        if (StringUtils.hasText(req.gender())) {
//            conditions.add(MEMBER.GENDER.eq(req.gender()));
//        }
//
//        // 4. 요금제명 (다중 선택) -> PRODUCT 테이블 컬럼 사용
//        if (!CollectionUtils.isEmpty(req.planNames())) {
//            conditions.add(PRODUCT.NAME.in(req.planNames()));
//        }
//
//        // 5. 가입일 범위 (기간 검색)
//        if (req.joinDateStart() != null) {
//            // 시작일의 00:00:00 부터
//            conditions.add(MEMBER.JOIN_DATE.ge(req.joinDateStart().atStartOfDay()));
//        }
//        if (req.joinDateEnd() != null) {
//            // 종료일의 23:59:59 까지 (하루 전체 포함)
//            conditions.add(MEMBER.JOIN_DATE.le(req.joinDateEnd().atTime(LocalTime.MAX)));
//        }
//
//        // 6. 연령대 다중 필터링 (생년월일 역산 로직)
//        // 요청: [20, 40] (20대 또는 40대 검색)
//        // 쿼리 변환: (20대 조건) OR (40대 조건)
//        if (!CollectionUtils.isEmpty(req.ageGroups())) {
//            Condition ageCondition = DSL.noCondition(); // 빈 조건으로 시작
//
//            for (Integer ageStart : req.ageGroups()) {
//                // 예: ageStart = 20 (20대)
//                // 2024년 기준 20세: 2004년생 ~ 1995년생 (29세)
//
//                LocalDate today = LocalDate.now();
//                // 20대 중 가장 늦게 태어난 날짜 (2004년생): 오늘 - 20년
//                LocalDate maxBirth = today.minusYears(ageStart);
//                // 20대 중 가장 일찍 태어난 날짜 (1995년생): 오늘 - 30년 + 1일
//                LocalDate minBirth = today.minusYears(ageStart + 10).plusDays(1);
//
//                // OR 조건으로 연결: ageCondition OR (birthDate BETWEEN min AND max)
//                ageCondition = ageCondition.or(MEMBER.BIRTH_DATE.between(minBirth, maxBirth));
//            }
//
//            // 완성된 연령대 조건을 전체 조건 리스트에 추가 (괄호로 묶임)
//            conditions.add(ageCondition);
//        }
//
//        return conditions;
//    }
//}