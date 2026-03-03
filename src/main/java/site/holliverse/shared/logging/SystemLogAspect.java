package site.holliverse.shared.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SystemLogAspect {
    private static final Logger SYSTEM_EVENT_LOG = LoggerFactory.getLogger("SYSTEM_EVENT");
    private static final int MAX_LOG_MESSAGE_LENGTH = 5000;

    @Around("@annotation(systemLogEvent)")
    public Object logAround(ProceedingJoinPoint joinPoint, SystemLogEvent systemLogEvent) throws Throwable {
        long start = System.currentTimeMillis();

        MDC.put(LogFieldKeys.EVENT, systemLogEvent.value());
        try {
            Object result = joinPoint.proceed();
            //비즈니스 로직 성공한 경우
            MDC.put(LogFieldKeys.OUTCOME, "success");
            MDC.put(LogFieldKeys.DURATION_MS, String.valueOf(System.currentTimeMillis() - start));
            SYSTEM_EVENT_LOG.info(limit("system event success: " + joinPoint.getSignature().toShortString()));

            return result;
        } catch (Throwable ex) {
            //비즈니스 로직 실패한 경우
            MDC.put(LogFieldKeys.OUTCOME, "failed");
            MDC.put(LogFieldKeys.DURATION_MS, String.valueOf(System.currentTimeMillis() - start));
            MDC.put(LogFieldKeys.ERROR_TYPE, ex.getClass().getName());
            SYSTEM_EVENT_LOG.warn(limit("system event failed: " + joinPoint.getSignature().toShortString()));
            throw ex;
        } finally {
            MDC.remove(LogFieldKeys.EVENT);
            MDC.remove(LogFieldKeys.OUTCOME);
            MDC.remove(LogFieldKeys.DURATION_MS);
            MDC.remove(LogFieldKeys.ERROR_TYPE);
        }
    }

    private String limit(String message) {
        if (message == null || message.isBlank()) return "-";
        if (message.length() <= MAX_LOG_MESSAGE_LENGTH) return message;
        return message.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...";
    }
}
