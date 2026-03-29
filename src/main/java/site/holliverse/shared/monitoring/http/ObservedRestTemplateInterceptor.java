package site.holliverse.shared.monitoring.http;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RestTemplate outbound 호출을 공통 형식으로 계측
 */
public class ObservedRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final MeterRegistry meterRegistry;
    private final String clientName;
    private final Map<TimerKey, Timer> timerCache = new ConcurrentHashMap<>();
    private final Map<ErrorCounterKey, Counter> errorCounterCache = new ConcurrentHashMap<>();

    public ObservedRestTemplateInterceptor(MeterRegistry meterRegistry, String clientName) {
        this.meterRegistry = meterRegistry;
        this.clientName = clientName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = normalizePath(request);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            String status = readStatus(response);
            String outcome = resolveOutcome(status);
            stop(sample, method, path, status, outcome);
            if (!"success".equals(outcome)) {
                incrementError(method, path, status, "http_status");
            }
            return response;
        } catch (IOException | RuntimeException ex) {
            stop(sample, method, path, "IO_ERROR", "error");
            incrementError(method, path, "IO_ERROR", ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private void stop(Timer.Sample sample, String method, String path, String status, String outcome) {
        sample.stop(timer(method, path, status, outcome));
    }

    private void incrementError(String method, String path, String status, String reason) {
        errorCounter(method, path, status, reason).increment();
    }

    private Timer timer(String method, String path, String status, String outcome) {
        TimerKey key = new TimerKey(method, path, status, outcome);
        return timerCache.computeIfAbsent(key, ignored ->
                Timer.builder("holliverse.outbound.http.duration")
                        .description("Outbound HTTP duration by client and path")
                        .tag("client", clientName)
                        .tag("method", method)
                        .tag("path", path)
                        .tag("status", status)
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Counter errorCounter(String method, String path, String status, String reason) {
        ErrorCounterKey key = new ErrorCounterKey(method, path, status, reason);
        return errorCounterCache.computeIfAbsent(key, ignored ->
                Counter.builder("holliverse.outbound.http.errors")
                        .description("Outbound HTTP errors by client and path")
                        .tag("client", clientName)
                        .tag("method", method)
                        .tag("path", path)
                        .tag("status", status)
                        .tag("reason", reason)
                        .register(meterRegistry));
    }

    private String normalizePath(HttpRequest request) {
        if (request.getURI() == null || request.getURI().getPath() == null || request.getURI().getPath().isBlank()) {
            return "/";
        }
        return request.getURI().getPath();
    }

    private String readStatus(ClientHttpResponse response) {
        try {
            return String.valueOf(response.getStatusCode().value());
        } catch (IOException ignored) {
            return "UNKNOWN";
        }
    }

    private String resolveOutcome(String status) {
        if (status.startsWith("2")) {
            return "success";
        }
        if (status.startsWith("4")) {
            return "client_error";
        }
        if (status.startsWith("5")) {
            return "server_error";
        }
        return "error";
    }

    private record TimerKey(String method, String path, String status, String outcome) {
    }

    private record ErrorCounterKey(String method, String path, String status, String reason) {
    }
}
