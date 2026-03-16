package site.holliverse.admin.domain.model.churn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 점수 계산에 사용하는 세부 signal 유형.
 */
@Getter
@RequiredArgsConstructor
public enum ChurnSignalType {
    // 계약 기반
    CONTRACT_REMAINING_WEEKS(ChurnFeatureType.CONTRACT, ChurnFeatureCollectionType.DEFAULT),
    TENURE_WEEKS(ChurnFeatureType.CONTRACT, ChurnFeatureCollectionType.DEFAULT),

    // 이용 요금
    ALLOWANCE_USAGE_RATE_PCT(ChurnFeatureType.USAGE, ChurnFeatureCollectionType.DEFAULT),

    // 사용자 행동
    CHANGE_MOBILE_COUNT(ChurnFeatureType.MEMBER_ACTION, ChurnFeatureCollectionType.DEFAULT),
    COMPARISON_COUNT(ChurnFeatureType.MEMBER_ACTION, ChurnFeatureCollectionType.REALTIME),
    CHECKED_PENALTY_FEE_COUNT(ChurnFeatureType.MEMBER_ACTION, ChurnFeatureCollectionType.REALTIME),

    // 고객 불만
    STAR_MEAN_SCORE(ChurnFeatureType.MEMBER_DISSATISFACTION, ChurnFeatureCollectionType.DEFAULT),
    CONSULTATION_SENTIMENT(ChurnFeatureType.MEMBER_DISSATISFACTION, ChurnFeatureCollectionType.EVENT),
    MAX_KEYWORD_NEGATIVE_WEIGHT(ChurnFeatureType.MEMBER_DISSATISFACTION, ChurnFeatureCollectionType.EVENT);

    private final ChurnFeatureType featureType;
    private final ChurnFeatureCollectionType collectionType;
}
