package site.holliverse.admin.web.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminMemberBulkStatusUpdateRequestDto(

        // 1. 빈 배열([])이나 null이 들어오는 것을 방지
        // 2. 한 번에 너무 많은 ID가 들어와서 DB에 무리를 주는 것을 방지 (최대 100개 제한)
        @NotEmpty(message = "상태를 변경할 회원 ID 목록은 비어있을 수 없습니다.")
        @Size(max = 100, message = "한 번에 최대 100명까지만 상태를 변경할 수 있습니다.")
        List<Long> memberIds,

        // 3. 상태값이 비어있는지 확인
        // 4. 정규식을 활용하여 이상한 상태값 차단
        @NotBlank(message = "변경할 상태값을 입력해주세요.")
        @Pattern(regexp = "^(ACTIVE|BANNED|DELETED|PROCESSING)$", message = "유효하지 않은 회원 상태값입니다.")
        String status
) {
}