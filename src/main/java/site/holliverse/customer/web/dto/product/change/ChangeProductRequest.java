package site.holliverse.customer.web.dto.product.change;

/**
 * 요금제 변경/신규 가입 API 요청 body.
 * memberId는 서버 인증 정보(세션/토큰)에서 추출하므로 클라이언트에서 보내지 않는다.
 */
public record ChangeProductRequest(
        Long targetProductId
) {}
