package site.holliverse.shared.alert.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.holliverse.shared.alert.config.AlertProperties;

@Service
public class AlertAuthService {

    private final AlertProperties alertProperties;

    public AlertAuthService(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public boolean isAuthorized(String providedSecret) {
        String configured = alertProperties.getRelaySecret();
        if (!StringUtils.hasText(configured)) {
            return false;
        }
        return configured.equals(providedSecret);
    }
}
