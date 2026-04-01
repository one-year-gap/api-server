package site.holliverse.customer.persistence.entity;

public enum UserLogDispatchStatus {
    READY,
    PROCESSING,
    ACKED,
    RETRY,
    DEAD
}
