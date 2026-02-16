package site.holliverse.customer.application.usecase.compare;

import site.holliverse.customer.application.usecase.dto.BenefitChangeItemDto;
import site.holliverse.customer.application.usecase.dto.ComparisonResultDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 모바일 요금제 비교: 가격 차이 + 필드별 변경 내역 계산.
 * - 가격: target.salePrice - current.salePrice
 * - Set 파싱: benefit_brands, benefit_media, benefit_premium → 추가/제거 집합
 * - 일반 문자열: data_amount, benefit_voice_call, benefit_sms, tethering_sharing_data, benefit_signature_family_discount
 */
@Component
public class PlanComparator {

    private static final String NONE = "없음";

    public ComparisonResultDto compare(ProductSummaryDto currentSummary, MobilePlanDetailDto currentPlan,
                                      ProductSummaryDto targetSummary, MobilePlanDetailDto targetPlan) {
        int priceDiff = targetSummary.salePrice() - currentSummary.salePrice();
        String message = formatPriceMessage(priceDiff);

        List<BenefitChangeItemDto> changes = new ArrayList<>();

        // 1. 데이터
        addStringChange(changes, "데이터",
                norm(currentPlan.dataAmount()),
                norm(targetPlan.dataAmount()));

        // 2. 통화
        addStringChange(changes, "통화",
                norm(currentPlan.benefitVoiceCall()),
                norm(targetPlan.benefitVoiceCall()));

        // 3. 문자
        addStringChange(changes, "문자",
                norm(currentPlan.benefitSms()),
                norm(targetPlan.benefitSms()));

        // 4. 테더링/쉐어링
        addStringChange(changes, "테더링",
                norm(currentPlan.tetheringSharingData()),
                norm(targetPlan.tetheringSharingData()));

        // 5. 혜택 (브랜드+미디어+프리미엄 통합, Set 파싱)
        addSetBenefitChange(changes, "혜택",
                currentPlan.benefitBrands(), currentPlan.benefitMedia(), currentPlan.benefitPremium(),
                targetPlan.benefitBrands(), targetPlan.benefitMedia(), targetPlan.benefitPremium());

        // 6. 가족할인
        addStringChange(changes, "가족할인",
                norm(currentPlan.benefitSignatureFamilyDiscount()),
                norm(targetPlan.benefitSignatureFamilyDiscount()));

        return new ComparisonResultDto(priceDiff, message, changes);
    }

    private static String norm(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return value.trim();
    }

    private static String formatPriceMessage(int priceDiff) {
        if (priceDiff == 0) {
            return "동일";
        }
        String formatted = String.format("%,d", Math.abs(priceDiff));
        return priceDiff > 0 ? "+" + formatted + "원" : "-" + formatted + "원";
    }

    private static void addStringChange(List<BenefitChangeItemDto> changes,
                                        String item, String currentVal, String targetVal) {
        boolean changed = !currentVal.equals(targetVal);
        String desc = changed ? currentVal + " → " + targetVal : "동일";
        changes.add(BenefitChangeItemDto.of(item, changed, desc));
    }

    private static void addSetBenefitChange(List<BenefitChangeItemDto> changes, String item,
                                            String currentBrands, String currentMedia, String currentPremium,
                                            String targetBrands, String targetMedia, String targetPremium) {
        Set<String> currentSet = toSet(currentBrands, currentMedia, currentPremium);
        Set<String> targetSet = toSet(targetBrands, targetMedia, targetPremium);

        Set<String> added = new LinkedHashSet<>(targetSet);
        added.removeAll(currentSet);
        Set<String> removed = new LinkedHashSet<>(currentSet);
        removed.removeAll(targetSet);

        boolean changed = !added.isEmpty() || !removed.isEmpty();
        String desc = buildSetChangeDesc(added, removed);
        List<String> addedList = new ArrayList<>(added);
        List<String> removedList = new ArrayList<>(removed);
        changes.add(BenefitChangeItemDto.of(item, changed, desc, addedList, removedList));
    }

    private static Set<String> toSet(String brands, String media, String premium) {
        Set<String> set = new LinkedHashSet<>();
        addSplit(set, brands);
        addSplit(set, media);
        addSplit(set, premium);
        return set;
    }

    private static void addSplit(Set<String> set, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(set::add);
    }

    private static String buildSetChangeDesc(Set<String> added, Set<String> removed) {
        if (added.isEmpty() && removed.isEmpty()) {
            return "동일";
        }
        List<String> parts = new ArrayList<>();
        if (!added.isEmpty()) {
            parts.add("추가: " + String.join(", ", added));
        }
        if (!removed.isEmpty()) {
            parts.add("제외: " + String.join(", ", removed));
        }
        return String.join(" / ", parts);
    }
}
