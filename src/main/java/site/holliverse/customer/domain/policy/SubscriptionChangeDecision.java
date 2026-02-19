package site.holliverse.customer.domain.policy;

/**
 * 구독 변경 정책의 의사결정 결과.
 * Application 계층에서 이 값에 따라 기존 구독 해지·신규 구독 생성 등 엔티티 조작을 수행한다.
 */
public record SubscriptionChangeDecision(
        boolean deactivateCurrent,
        boolean createNew
) {}
