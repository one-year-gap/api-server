package site.holliverse.customer.application.usecase.compare;

import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.shared.domain.model.ProductType;

/**
 * PlanComparatorTest용 실제 요금제 데이터 (5G 프리미어 에센셜 / 5G 프리미어 플러스).
 * 시트 기준으로 정리한 값 사용.
 */
public final class PlanComparatorTestData {

    private static final Long ESSENTIAL_PRODUCT_ID = 1L;
    private static final Long PLUS_PRODUCT_ID = 2L;

    private PlanComparatorTestData() {}

    // ---------- 5G 프리미어 에센셜 ----------
    // name: 5G 프리미어 에센셜
    // price: 월 85,000원 → 85000
    // sale_price: 약정 할인 시 월 58,500원 → 58500
    // data_amount: 데이터 무제한
    // tethering: 테더링 + 쉐어링 70GB
    // brands: (없음)
    // voice: 집/이동전화 무제한 (+부가통화 300분)
    // sms: 기본제공
    // media, premium, family_discount: (없음)

    public static ProductSummaryDto essentialSummary() {
        return new ProductSummaryDto(
                ESSENTIAL_PRODUCT_ID,
                "5G 프리미어 에센셜",
                85_000,
                58_500,
                ProductType.MOBILE_PLAN,
                "5G-PREMIER-ESSENTIAL",
                "약정 할인"
        );
    }

    public static MobilePlanDetailDto essentialMobilePlan() {
        return new MobilePlanDetailDto(
                ESSENTIAL_PRODUCT_ID,
                "데이터 무제한",
                "테더링 + 쉐어링 70GB",
                null,
                "집/이동전화 무제한 (+부가통화 300분)",
                "기본제공",
                null,
                null,
                null
        );
    }

    // ---------- 5G 프리미어 플러스 ----------
    // name: 5G 프리미어 플러스
    // price: 월 105,000원 → 105000
    // sale_price: 약정 할인 시 월 73,500원 → 73500
    // data_amount: 데이터 무제한
    // tethering: 테더링+쉐어링 100GB
    // brands: 넷플릭스 | 유튜브 프리미엄 | 디즈니+ | 티빙 | 멀티팩
    // voice: 집/이동전화 무제한 (+부가통화 300분)
    // sms: 기본제공
    // media: 콘텐츠, 음악 감상 등 최대 11,900원 혜택
    // premium: OTT, 구독 등 최대 월 23,900원 혜택
    // family_discount: (없음)

    public static ProductSummaryDto plusSummary() {
        return new ProductSummaryDto(
                PLUS_PRODUCT_ID,
                "5G 프리미어 플러스",
                105_000,
                73_500,
                ProductType.MOBILE_PLAN,
                "5G-PREMIER-PLUS",
                "약정 할인"
        );
    }

    public static MobilePlanDetailDto plusMobilePlan() {
        return new MobilePlanDetailDto(
                PLUS_PRODUCT_ID,
                "데이터 무제한",
                "테더링+쉐어링 100GB",
                "넷플릭스 | 유튜브 프리미엄 | 디즈니+ | 티빙 | 멀티팩",
                "집/이동전화 무제한 (+부가통화 300분)",
                "기본제공",
                "콘텐츠, 음악 감상 등 최대 11,900원 혜택",
                "OTT, 구독 등 최대 월 23,900원 혜택",
                null
        );
    }
}
