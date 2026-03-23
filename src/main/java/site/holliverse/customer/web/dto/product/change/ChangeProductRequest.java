package site.holliverse.customer.web.dto.product.change;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 요금제 변경/신규 가입 API 요청 body.
 * memberId는 서버 인증 정보(세션/토큰)에서 추출하므로 클라이언트에서 보내지 않는다.
 */
public record ChangeProductRequest(
        @NotNull(message = "targetProductId는 필수 값입니다.")
        @Positive(message = "targetProductId는 1 이상이어야 합니다.")
        Long targetProductId
) {}
