package site.holliverse.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import site.holliverse.shared.logging.LogStaticContext;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 공통 요청 처리
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCRequestLoggingFilter extends OncePerRequestFilter {

    //요청 시작 시간
    public static final String REQUEST_START_AT_ATTRIBUTE = MDCRequestLoggingFilter.class.getName() + ".startAt";

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String XRAY_TRACE_HEADER = "X-Amzn-Trace-Id";
    private static final String B3_TRACE_HEADER = "X-B3-TraceId";
    private static final String TRACE_PARENT_HEADER = "traceparent";

    private final LogStaticConstatext logStaticContext;

    public MDCRequestLoggingFilter(
            LogStaticContext logStaticContext,
            @Value("${app.env:unknown-env}") String env
    ) {
        this.logStaticContext = logStaticContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = resolveRequestId(request);
        String traceId = resolveTraceId(request, requestId);
        long startAtMillis = System.currentTimeMillis();

        //요청 시작 MDC
        MDC.put(LogFieldKeys.REQUEST_ID, requestId);
        MDC.put(LogFieldKeys.TRACE_ID, traceId);
        MDC.put(LogFieldKeys.METHOD, request.getMethod());
        MDC.put(LogFieldKeys.TIME_STAMP, String.valueOf(startAtMillis));
        MDC.put(LogFieldKeys.SERVICE, logStaticContext.service());
        MDC.put(LogFieldKeys.VERSION, logStaticContext.version());
        MDC.put(LogFieldKeys.TEAM, logStaticContext.team());

        request.setAttribute(REQUEST_START_AT_ATTRIBUTE, startAtMillis);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startAtMillis;
            //요청 종료 MDC
            MDC.put(LogFieldKeys.STATUS, String.valueOf(response.getStatus()));
            MDC.put(LogFieldKeys.DURATION_MS, String.valueOf(durationMs));
            MDC.put(LogFieldKeys.URI_TEMPLATE, resolveUriTemplate(request));

            if (!isAccessLogExcluded(request)) {
                accessLog.info("http request completed");
            }

            MDC.clear();
        }
    }

    /**
     * health check & prometheus log는 제외
     */
    private boolean isAccessLogExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && (uri.startsWith("/actuator/health") || uri.equals("/actuator/prometheus"));
    }

    /**
     * MDC에 URI 남아 있으면 사용하고 없으면 새로 읽기
     */
    private String resolveUriTemplate(HttpServletRequest request) {
        String fromMdc = MDC.get(LogFieldKeys.URI_TEMPLATE);
        if (StringUtils.hasText(fromMdc)) {
            return fromMdc;
        }

        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null && StringUtils.hasText(pattern.toString())) {
            return pattern.toString();
        }

        return "unknown";
    }

    /**
     * HTTP 요청 ID 추출
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
    }

    /**
     * HTTP 요청 TraceID 추출
     * X-Amzn-Trace-Id -> X-B3-TraceId -> traceparent 순으로 추출
     */
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
