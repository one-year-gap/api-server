package site.holliverse.shared.alert.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import site.holliverse.shared.alert.service.AlertAuthService;
import site.holliverse.shared.alert.service.AlertIncidentService;
import site.holliverse.shared.alert.web.dto.AlertAckRequest;
import site.holliverse.shared.web.response.ApiResponse;

@RestController
@RequestMapping("/internal/alerts/incidents")
public class AlertIncidentCommandController {

    private final AlertAuthService alertAuthService;
    private final AlertIncidentService alertIncidentService;

    public AlertIncidentCommandController(AlertAuthService alertAuthService,
                                          AlertIncidentService alertIncidentService) {
        this.alertAuthService = alertAuthService;
        this.alertIncidentService = alertIncidentService;
    }

    @PostMapping("/{incidentId}/ack")
    public ApiResponse<Void> ackIncident(
            @RequestHeader(name = "X-Alert-Secret", required = false) String secret,
            @PathVariable Long incidentId,
            @RequestBody AlertAckRequest request
    ) {
        authorize(secret);
        alertIncidentService.acknowledge(incidentId, request.actor(), request.comment(), request.runbookUrl());
        return ApiResponse.success("incident ack 처리 완료", null);
    }

    private void authorize(String providedSecret) {
        if (!alertAuthService.isAuthorized(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid alert secret");
        }
    }
}
