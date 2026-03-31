package site.holliverse.customer.application.usecase.log;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.holliverse.customer.integration.external.AdminLogFeaturesClient;

/**
 * admin log-feature 별도 executor로 분리
 */
@Service
@Profile("customer")
@RequiredArgsConstructor
public class AdminLogFeatureDispatchService {

    private final AdminLogFeaturesClient adminLogFeaturesClient;

    @Async("adminLogFeatureTaskExecutor")
    public void dispatch(Long memberId, UserLogEventName eventName, String timestamp) {
        adminLogFeaturesClient.sendLogFeature(memberId, eventName, timestamp);
    }
}
