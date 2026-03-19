package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.holliverse.admin.config.SolapiProperties;
import site.holliverse.admin.integration.sms.SolapiSmsClient;
import site.holliverse.admin.query.dao.AdminChurnCouponDao;
import site.holliverse.admin.query.dao.CouponSmsTargetRawData;
import site.holliverse.shared.util.DecryptionTool;

import java.util.Collections;
import java.util.List;

@Slf4j
@Profile("admin")
@Service
@RequiredArgsConstructor
public class ChurnCouponSmsService {

    private static final String COUPON_MESSAGE = "[HSC] 고객님 다시 보고싶습니다 쿠폰이 발급 되었어요. 앱에서 확인해 주세요." +
            "링크 : https://holliverse.site/login";

    private final AdminChurnCouponDao adminChurnCouponDao;
    private final DecryptionTool decryptionTool;
    private final SolapiSmsClient solapiSmsClient;
    private final SolapiProperties solapiProperties;

    public void sendCouponIssuedMessages(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        List<CouponSmsTargetRawData> targets = adminChurnCouponDao.findCouponSmsTargets(memberIds);
        List<String> allowedRecipients = targets.stream()
                .map(this::extractAllowedPhone)
                .filter(phone -> phone != null && !phone.isBlank())
                .toList();

        if (allowedRecipients.isEmpty()) {
            return;
        }

        solapiSmsClient.sendCouponIssuedMessage(allowedRecipients, COUPON_MESSAGE);
    }

    private String extractAllowedPhone(CouponSmsTargetRawData target) {
        try {
            String phone = decryptionTool.decrypt(target.encryptedPhone());
            if (phone == null || phone.isBlank()) {
                log.warn("[ChurnCouponSms] phone missing. skip sms. memberId={}", target.memberId());
                return null;
            }

            String normalizedPhone = phone.replace("-", "");
            if (!isAllowedTestPhone(normalizedPhone)) {
                log.warn("[ChurnCouponSms] phone not allowed for test sending. skip sms. memberId={}", target.memberId());
                return null;
            }

            return normalizedPhone;
        } catch (Exception e) {
            log.warn("[ChurnCouponSms] sms target processing failed. memberId={}", target.memberId(), e);
            return null;
        }
    }

    private boolean isAllowedTestPhone(String phone) {
        List<String> allowedPhones = solapiProperties.allowedTestPhones() == null
                ? Collections.emptyList()
                : solapiProperties.allowedTestPhones();

        return allowedPhones.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.replace("-", ""))
                .anyMatch(phone::equals);
    }
}
