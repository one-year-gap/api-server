package site.holliverse.admin.query.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor // jOOQ가 데이터를 채워줄 때 필요
public class MemberRawData {
    // 1. 식별자 (상세 클릭 시 필요)
    private Long id;

    // 2. 등급
    private String membership;

    // 3. 성별
    private String gender;

    // 4. 이름 (암호화된 상태)
    private String encryptedName;

    // 5. 생년월일
    private LocalDate birthDate;

    // 6. 연락처 (암호화된 상태)
    private String encryptedPhone;

    // 7. 이메일
    private String email;

    // 8. 이용 요금제 (Product 테이블에서 가져온 이름)
    private String planName;

    // 9. 회원 상태
    private String status;
}