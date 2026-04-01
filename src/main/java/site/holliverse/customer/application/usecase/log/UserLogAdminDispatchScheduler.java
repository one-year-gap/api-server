package site.holliverse.customer.application.usecase.log;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("customer")
@RequiredArgsConstructor
public class UserLogAdminDispatchScheduler {

    private final UserLogAdminDispatchOutboxService outboxService;

    @Value("${app.userlog.admin-dispatch.batch-size:100}")
    private int batchSize;

    @Scheduled(
            initialDelayString = "${app.userlog.admin-dispatch.initial-delay-ms:5000}",
            fixedDelayString = "${app.userlog.admin-dispatch.fixed-delay-ms:3000}"
    )
    public void dispatch() {
        outboxService.dispatchReadyBatch(batchSize);
    }
}
