package site.holliverse.admin.domain.model.churn;

import site.holliverse.admin.domain.model.churn.feature.ChurnFeature;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * 회원 기준 feature 묶음.
 */
public record ChurnFeatureSet(
        Map<ChurnFeatureType, ChurnFeature> features
) {

    public ChurnFeatureSet {
        features = features == null
                ? Map.of()
                : Map.copyOf(features);
    }

    public static ChurnFeatureSet empty() {
        return new ChurnFeatureSet(new EnumMap<>(ChurnFeatureType.class));
    }

    public Optional<ChurnFeature> get(ChurnFeatureType type) {
        return Optional.ofNullable(features.get(type));
    }
}
