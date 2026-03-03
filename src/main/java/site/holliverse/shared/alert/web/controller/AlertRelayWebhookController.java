package site.holliverse.shared.alert.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.shared.alert.service.AlertAuthService;
import site.holliverse.shared.alert.service.GrafanaAlertRelayService;
import site.holliverse.shared.alert.web.dto.GrafanaAlertWebhookRequest;
import site.holliverse.shared.web.response.ApiResponse;

@RestController
@RequestMapping("/internal/alerts")
public class AlertRelayWebhookController {

    private final AlertAuthService alertAuthService;
    private final GrafanaAlertRelayService grafanaAlertRelayService;

    public AlertRelayWebhookController(AlertAuthService alertAuthService,
                                       GrafanaAlertRelayService grafanaAlertRelayService) {
        this.alertAuthService = alertAuthService;
        this.grafanaAlertRelayService = grafanaAlertRelayService;
    }

    @PostMapping("/grafana")
    public ApiResponse<Void> receiveGrafanaAlert(
            @RequestHeader(name = "X-Alert-Secret", required = false) String secret,
            @RequestBody GrafanaAlertWebhookRequest request
    ) {
        authorize(secret);
        grafanaAlertRelayService.handleWebhook(request);
        return ApiResponse.success("grafana alert received", null);
    }

    private void authorize(String providedSecret) {
        if (!alertAuthService.isAuthorized(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid alert secret");
        }
    }
}
