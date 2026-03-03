package site.holliverse.shared.config.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.holliverse.shared.logging.LogFieldKeys;
import site.holliverse.shared.logging.LogStaticContext;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestMdcFilter extends OncePerRequestFilter {

    public static final String REQUEST_START_AT_ATTRIBUTE = RequestMdcFilter.class.getName() + ".startAt";

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String XRAY_TRACE_HEADER = "X-Amzn-Trace-Id";
    private static final String B3_TRACE_HEADER = "X-B3-TraceId";
    private static final String TRACE_PARENT_HEADER = "traceparent";
    private static final String ANONYMOUS_MEMBER = "anonymous";

    private final LogStaticContext logStaticContext;

    public RequestMdcFilter(LogStaticContext logStaticContext) {
        this.logStaticContext = logStaticContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        long startAtMillis = System.currentTimeMillis();
        String traceId = resolveTraceId(request, requestId);

        MDC.put(LogFieldKeys.REQUEST_ID, requestId);
        MDC.put(LogFieldKeys.TRACE_ID, traceId);
        MDC.put(LogFieldKeys.METHOD, request.getMethod());
        MDC.put(LogFieldKeys.RAW_PATH, request.getRequestURI());
        MDC.put(LogFieldKeys.START_AT, String.valueOf(startAtMillis));
        MDC.put(LogFieldKeys.MEMBER_KEY_HASH, ANONYMOUS_MEMBER);
        MDC.put(LogFieldKeys.TEAM, logStaticContext.team());

        request.setAttribute(REQUEST_START_AT_ATTRIBUTE, startAtMillis);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
    }

    private String resolveTraceId(HttpServletRequest request, String fallback) {
        String xrayTraceId = request.getHeader(XRAY_TRACE_HEADER);
        if (StringUtils.hasText(xrayTraceId)) {
            return xrayTraceId;
        }

        String b3TraceId = request.getHeader(B3_TRACE_HEADER);
        if (StringUtils.hasText(b3TraceId)) {
            return b3TraceId;
        }

        String traceParent = request.getHeader(TRACE_PARENT_HEADER);
        if (StringUtils.hasText(traceParent)) {
            String[] parts = traceParent.split("-");
            if (parts.length >= 4 && StringUtils.hasText(parts[1])) {
                return parts[1];
            }
        }

        return fallback;
    }
}
