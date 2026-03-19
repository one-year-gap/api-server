package site.holliverse.shared.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 컨트롤러/유스케이스/DAO/연동 계층 공통 실행 시간을 기록
 */
@Aspect
@Component
public class OperationMonitoringAspect {

    private final MeterRegistry meterRegistry;

    public OperationMonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around(
            "execution(public * site.holliverse..web.controller..*(..))" +
            " || execution(public * site.holliverse..application.usecase..*(..))" +
            " || execution(public * site.holliverse..query.dao..*(..))" +
            " || execution(public * site.holliverse..integration..*(..))"
    )
    public Object observe(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String declaringType = signature.getDeclaringTypeName();
        String component = signature.getDeclaringType().getSimpleName();
        String method = signature.getMethod().getName();
        String layer = resolveLayer(declaringType);

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = joinPoint.proceed();
            stop(sample, layer, component, method, "success");
            return result;
        } catch (Throwable throwable) {
            stop(sample, layer, component, method, "error");
            Counter.builder("holliverse.operation.errors")
                    .description("Count of failed operations by layer/component/method")
                    .tag("layer", layer)
                    .tag("component", component)
                    .tag("method", method)
                    .tag("exception", throwable.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            throw throwable;
        }
    }

    private void stop(Timer.Sample sample, String layer, String component, String method, String outcome) {
        sample.stop(
                Timer.builder("holliverse.operation.duration")
                        .description("Execution time for controllers, use cases, DAOs, and integration clients")
                        .tag("layer", layer)
                        .tag("component", component)
                        .tag("method", method)
                        .tag("outcome", outcome)
                        .register(meterRegistry)
        );
    }

    private String resolveLayer(String declaringType) {
        if (declaringType.contains(".web.controller.")) {
            return "controller";
        }
        if (declaringType.contains(".application.usecase.")) {
            return "usecase";
        }
        if (declaringType.contains(".query.dao.")) {
            return "dao";
        }
        if (declaringType.contains(".integration.")) {
            return "integration";
        }
        return "other";
    }
}
