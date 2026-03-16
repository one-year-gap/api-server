package site.holliverse.admin.query.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DAO가 jOOQ 조회 결과를 바로 담아오는 중간 전달 객체.
 *
 * 이 객체는 "DB 원본값"을 담는 용도이므로 다음 특징이 있다.
 * - 이름/전화번호는 복호화 전 상태(암호문) 그대로 보관
 * - risk_reasons는 JSONB를 String으로 캐스팅한 원본 문자열로 보관
 * - 화면 표시용 마스킹, JSON 배열 파싱 등은 여기서 하지 않음
 *
 * 이 값은 Assembler가 담담ㅇ
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChurnRiskMemberRawData {

    private Long memberId;

    private String membership;

    private String encryptedName;

    private String riskLevel;

    private Integer churnScore;

    private String riskReasons;

    private String encryptedPhone;

    private String email;
}
