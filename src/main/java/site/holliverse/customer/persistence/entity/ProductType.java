package site.holliverse.customer.persistence.entity;

/**
 * 상품(요금제) 카테고리.
 * 목록 조회 쿼리 파라미터 category (mobile | internet | iptv | add-on)와 매핑 시 변환 사용.
 */
public enum ProductType {
    MOBILE_PLAN,
    INTERNET,
    IPTV,
    TAB_WATCH_PLAN,
    ADDON
}
