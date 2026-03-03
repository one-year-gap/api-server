package site.holliverse.shared.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import site.holliverse.shared.security.CustomUserDetails;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class RequestMdcInterceptor implements HandlerInterceptor {
    private static final String UNDEFINED_MEMBER = "unknown"; //비회원 유저

    private final String memberHashSalt;

    public RequestMdcInterceptor(
            @Value("${app.logging.member-hash-salt}") String memberHashSalt
    ) {
        this.memberHashSalt = memberHashSalt;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object uriTemplate = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        MDC.put(
                LogFieldKeys.URI_TEMPLATE,
                uriTemplate != null ? uriTemplate.toString() : "unknown"
        );
        MDC.put(LogFieldKeys.MEMBER_KEY_HASH, resolveMemberKeyHash());
        return true;
    }

    // status/duration은 Filter에서 처리 -> Interceptor는 이 책임 제외
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // no-op
    }

    private String resolveMemberKeyHash() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //인증 정보 없으면 default memberId
        if (authentication == null || !authentication.isAuthenticated()) {
            return UNDEFINED_MEMBER;
        }

        //memberId 추출
        Long memberId = extractMemberId(authentication.getPrincipal());
        if (memberId == null) {
            return UNDEFINED_MEMBER;
        }

        // salted hash
        return sha256Hex(memberHashSalt + ":" + memberId);
    }

    /**
     * Spring Security CustomUserDetails 이용 -> memberId 추출
     */
    private Long extractMemberId(Object principal) {
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getMemberId();
        }
        if (principal instanceof OAuth2User oauth2User) {
            Object memberId = oauth2User.getAttribute("memberId");
            if (memberId instanceof Number number) {
                return number.longValue();
            }
            if (memberId instanceof String text) {
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 평문 SHA-256 암호화
     */
    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
