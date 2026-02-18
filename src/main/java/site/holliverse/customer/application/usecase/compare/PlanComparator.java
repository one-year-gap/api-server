package site.holliverse.customer.application.usecase.compare;

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
    private static final String LABEL_SAME = "동일";
    private static final String SUFFIX_CURRENCY = "원";

    /** 혜택 항목명 (할인/스펙 라벨) */
    private static final String ITEM_DATA = "데이터";
    private static final String ITEM_VOICE = "통화";
    private static final String ITEM_SMS = "문자";
    private static final String ITEM_TETHERING = "테더링";
    private static final String ITEM_BENEFIT = "혜택";
    private static final String ITEM_FAMILY_DISCOUNT = "가족할인";

    private static final String PREFIX_ADDED = "추가: ";
    private static final String PREFIX_REMOVED = "제외: ";
    private static final String SEPARATOR_PARTS = " / ";

    public ComparisonResultDto compare(ProductSummaryDto currentSummary, MobilePlanDetailDto currentPlan,
                                      ProductSummaryDto targetSummary, MobilePlanDetailDto targetPlan) {
        int priceDiff = targetSummary.salePrice() - currentSummary.salePrice();
        String message = formatPriceMessage(priceDiff);

        List<BenefitChangeItemDto> changes = new ArrayList<>();

        addStringChange(changes, ITEM_DATA,
                norm(currentPlan.dataAmount()),
                norm(targetPlan.dataAmount()));
        addStringChange(changes, ITEM_VOICE,
                norm(currentPlan.benefitVoiceCall()),
                norm(targetPlan.benefitVoiceCall()));
        addStringChange(changes, ITEM_SMS,
                norm(currentPlan.benefitSms()),
                norm(targetPlan.benefitSms()));
        addStringChange(changes, ITEM_TETHERING,
                norm(currentPlan.tetheringSharingData()),
                norm(targetPlan.tetheringSharingData()));
        addSetBenefitChange(changes, ITEM_BENEFIT,
                currentPlan.benefitBrands(), currentPlan.benefitMedia(), currentPlan.benefitPremium(),
                targetPlan.benefitBrands(), targetPlan.benefitMedia(), targetPlan.benefitPremium());
        addStringChange(changes, ITEM_FAMILY_DISCOUNT,
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
            return LABEL_SAME;
        }
        String formatted = String.format("%,d", Math.abs(priceDiff));
        return priceDiff > 0 ? "+" + formatted + SUFFIX_CURRENCY : "-" + formatted + SUFFIX_CURRENCY;
    }

    private static void addStringChange(List<BenefitChangeItemDto> changes,
                                        String item, String currentVal, String targetVal) {
        boolean changed = !currentVal.equals(targetVal);
        String desc = changed ? currentVal + " → " + targetVal : LABEL_SAME;
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
            return LABEL_SAME;
        }
        List<String> parts = new ArrayList<>();
        if (!added.isEmpty()) {
            parts.add(PREFIX_ADDED + String.join(", ", added));
        }
        if (!removed.isEmpty()) {
            parts.add(PREFIX_REMOVED + String.join(", ", removed));
        }
        return String.join(SEPARATOR_PARTS, parts);
    }
}
