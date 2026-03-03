package site.holliverse.shared.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogStaticContext {

    private final String service;
    private final String version;
    private final String team;

    public LogStaticContext(
            @Value("${spring.application.name:unknown-service}") String service,
            @Value("${app.version:unknown-version}") String version,
            @Value("${app.team:unknown-team}") String team
    ) {
        this.service = service;
        this.version = version;
        this.team = team;
    }

    public String service() {
        return service;
    }

    public String version() {
        return version;
    }

    public String team() {
        return team;
    }
}
