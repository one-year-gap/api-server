package site.holliverse.admin.web.dto.member;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdminMemberDto {
    // 1. 식별자
    private Long id;

    // 2. 등급
    private String membership;

    // 3. 성별
    private String gender;

    // 4. 이름 (복호화 완료된 평문)
    private String name;

    // 5. 생년월일
    private LocalDate birthDate;

    // 6. 연락처 (복호화 완료된 평문)
    private String phone;

    // 7. 이메일
    private String email;

    // 8. 이용 요금제
    private String planName;
}