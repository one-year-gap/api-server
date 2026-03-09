package site.holliverse.customer.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 프론트 전달용 혜택 문구 정규화.
 * 예: "집 / 이동전화 무제한 (+부가통화 300분)" → "부가 통화 300분"
 */
public final class BenefitDisplayUtil {

    private static final Pattern BONUS_VOICE_PATTERN = Pattern.compile(
            "(?s).*?\\(\\s*\\+?\\s*부가\\s*통화\\s*(\\d+)\\s*분\\s*\\).*"
    );

    private BenefitDisplayUtil() {}

    /**
     * 음성/문자 혜택 문구를 프론트 표시용으로 정규화.
     * "(+부가통화 N분)" 또는 "(부가통화 N분)" 패턴이 있으면 "부가 통화 N분"만 반환.
     */
    public static String normalizeBenefitForDisplay(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String trimmed = raw.trim();
        Matcher m = BONUS_VOICE_PATTERN.matcher(trimmed);
        if (m.matches()) {
            return "부가 통화 " + m.group(1) + "분";
        }
        return raw;
    }
}
