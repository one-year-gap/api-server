package site.holliverse.customer.web.dto.product.change;

/**
 * 요금제 변경/신규 가입 API 요청 body.
 */
public record ChangeProductRequest(
        Long memberId,
        Long targetProductId
) {}
