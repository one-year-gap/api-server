package site.holliverse.shared.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.security.CustomUserDetails;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class RequestMdcInterceptor implements HandlerInterceptor {

    private static final String ANONYMOUS_MEMBER = "anonymous";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object uriTemplate = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        MDC.put(LogFieldKeys.URI_TEMPLATE, uriTemplate != null ? uriTemplate.toString() : request.getRequestURI());
        MDC.put(LogFieldKeys.MEMBER_KEY_HASH, resolveMemberKeyHash());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.put(LogFieldKeys.STATUS, String.valueOf(response.getStatus()));
    }

    private String resolveMemberKeyHash() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ANONYMOUS_MEMBER;
        }

        Object principal = authentication.getPrincipal();
        Long memberId = extractMemberId(principal);
        if (memberId == null) {
            return ANONYMOUS_MEMBER;
        }
        return sha256Hex(memberId.toString());
    }

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
