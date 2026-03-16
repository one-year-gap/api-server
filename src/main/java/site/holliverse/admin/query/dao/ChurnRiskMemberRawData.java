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
 * 즉, "DB 조회 결과를 안전하게 담아두는 그릇" 역할만 수행하고,
 * 실제 화면 응답 형태로의 가공은 Assembler에서 담당한다.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChurnRiskMemberRawData {
    /** 회원 PK */
    private Long memberId;
    /** 회원 등급 (예: GOLD, VIP, VVIP) */
    private String membership;
    /** DB에 암호화되어 저장된 이름 */
    private String encryptedName;
    /** 이탈 위험도 (HIGH / MEDIUM / LOW) */
    private String riskLevel;
    /** 이탈 점수 정수값 */
    private Integer churnScore;
    /** JSON 배열 형태의 위험 사유 원본 문자열 */
    private String riskReasons;
    /** DB에 암호화되어 저장된 전화번호 */
    private String encryptedPhone;
    /** 회원 이메일 원문 */
    private String email;
}
