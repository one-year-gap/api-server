package site.holliverse.shared.alert.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import site.holliverse.shared.alert.model.AlertEnvelope;
import site.holliverse.shared.alert.persistence.entity.AlertIncident;
import site.holliverse.shared.alert.persistence.repository.AlertIncidentRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.time.LocalDateTime;

@Service
public class AlertIncidentService {

    private final AlertIncidentRepository alertIncidentRepository;

    public AlertIncidentService(AlertIncidentRepository alertIncidentRepository) {
        this.alertIncidentRepository = alertIncidentRepository;
    }

    @Transactional
    public AlertIncident recordFiring(AlertEnvelope alert) {
        LocalDateTime now = LocalDateTime.now();
        return alertIncidentRepository.findByFingerprint(alert.fingerprint())
                .map(existing -> {
                    existing.markFiredAgain(
                            alert.severity(),
                            alert.summary(),
                            alert.message(),
                            alert.grafanaAlertUrl(),
                            alert.runbookUrl(),
                            now
                    );
                    return existing;
                })
                .orElseGet(() -> alertIncidentRepository.save(AlertIncident.createNew(
                        alert.fingerprint(),
                        alert.severity(),
                        alert.owner(),
                        alert.team(),
                        alert.service(),
                        alert.version(),
                        alert.errorCode(),
                        alert.method(),
                        alert.uriTemplate(),
                        alert.alertName(),
                        alert.summary(),
                        alert.message(),
                        alert.grafanaAlertUrl(),
                        alert.runbookUrl(),
                        now
                )));
    }

    @Transactional
    public void acknowledge(Long incidentId, String actor, String comment, String runbookUrl) {
        AlertIncident incident = getIncident(incidentId);
        String normalizedActor = StringUtils.hasText(actor) ? actor : "unknown";
        incident.acknowledge(normalizedActor, comment, LocalDateTime.now());
        incident.updateRunbook(runbookUrl);
    }

    @Transactional
    public void markNotified(Long incidentId, LocalDateTime now) {
        AlertIncident incident = getIncident(incidentId);
        incident.markNotified(now);
    }

    private AlertIncident getIncident(Long incidentId) {
        return alertIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "incidentId", "존재하지 않는 알림 incident 입니다."));
    }
}
