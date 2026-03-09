package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.jooq.enums.DispatchStatus;

import static org.jooq.impl.DSL.currentLocalDateTime;
import static site.holliverse.admin.query.jooq.Tables.ANALYSIS_DISPATCH_OUTBOX;

/**
 * outbox RETRY 전이 서비스
 */
@Service
@Profile("admin")
@RequiredArgsConstructor
public class AnalysisDispatchOutboxRetryService {
    private final DSLContext dsl;

    /**
     * 본 트랜잭션 롤백과 분리된 새 트랜잭션(REQUIRES_NEW) RETRY 상태를 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(String requestId, String errorMessage) {
        dsl.update(ANALYSIS_DISPATCH_OUTBOX)
                .set(ANALYSIS_DISPATCH_OUTBOX.DISPATCH_STATUS, DispatchStatus.RETRY)
                .set(ANALYSIS_DISPATCH_OUTBOX.LAST_ERROR, errorMessage)
                .set(ANALYSIS_DISPATCH_OUTBOX.ATTEMPT_COUNT, ANALYSIS_DISPATCH_OUTBOX.ATTEMPT_COUNT.plus(1))
                .set(ANALYSIS_DISPATCH_OUTBOX.UPDATED_AT, currentLocalDateTime())
                .where(ANALYSIS_DISPATCH_OUTBOX.REQUEST_ID.eq(requestId))
                .execute();
    }
}
