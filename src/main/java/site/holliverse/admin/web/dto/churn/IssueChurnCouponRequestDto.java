package site.holliverse.admin.web.dto.churn;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 ==========================
 * $NAME
 * 쿠폰 요청 DTO
 * 쿠폰 발급 대상인 memberID 와 쿠폰 넘버가 들어간다.
 * @author nonstop
 * @version 1.0.0
 * @since 2026-03-16
 * ========================== */
public record IssueChurnCouponRequestDto(

        //받을 멤버 목록
        @NotEmpty(message = "쿠폰을 발송할 회원 ID 목록은 비어 있을 수 없습니다.")
        List<@NotNull(message = "회원 ID는 null 일 수 없습니다.") Long> memberIds,


        //쿠폰 아이디
        @NotNull(message = "쿠폰 ID는 필수 입니다.")
        @Min(value = 1, message ="쿠폰 ID는 1 이상이어야합니다.")
        Long couponId
) {


}
