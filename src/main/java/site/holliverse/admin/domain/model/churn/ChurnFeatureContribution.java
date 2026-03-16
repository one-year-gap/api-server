package site.holliverse.admin.domain.model.churn;

/**
 * signal별 점수 기여도.
 */
public record ChurnFeatureContribution(
        ChurnSignalType signalType,
        String observedValue,
        int appliedScore
) {

    public ChurnFeatureType featureType() {
        return signalType.getFeatureType();
    }

    public ChurnFeatureCollectionType collectionType() {
        return signalType.getCollectionType();
    }
}
