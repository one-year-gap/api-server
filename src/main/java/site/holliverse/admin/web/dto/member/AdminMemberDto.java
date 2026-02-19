package site.holliverse.admin.web.dto.member;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record AdminMemberDto (
    // 1. 식별자
    Long id,

    // 2. 등급
    String membership,

    // 3. 성별
    String gender,

    // 4. 이름 (복호화 완료된 평문)
    String name,

    // 5. 생년월일
    LocalDate birthDate,

    // 6. 연락처 (복호화 완료된 평문)
    String phone,

    // 7. 이메일
    String email,

    // 8. 이용 요금제
    String planName
) {}