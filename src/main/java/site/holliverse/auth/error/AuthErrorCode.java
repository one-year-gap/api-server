package site.holliverse.auth.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.holliverse.shared.error.ErrorCategory;
import site.holliverse.shared.error.ErrorCode;

/**
 * [Auth ErrorCode 규칙]
 * <p>
 * 1) 목적
 * - Auth Bounded Context 에서 발생하는 모든 예외/오류를 ErrorCode 표준화
 * - REST/API 응답, Batch 처리, Kafka 처리, Loki/Grafana 로그에서 동일한 code로 추적 가능해야 한다.
 * <p>
 * 2) code 네이밍 규칙
 * - 포맷: "{BC}-{CATEGORY}-{SEQ}"
 * - BC: AUTH = "AUT" (프로젝트 내 BC 약어 통일)
 * - CATEGORY: VAL | DOM | APP | INFRA | EXT 등
 * - SEQ: 3자리 일련번호 (001부터 증가)
 * - 예: "BIL-VAL-001"
 * <p>
 * 3) category 규칙 (ErrorCategory)
 * - VAL   : 입력/데이터 유효성 검증 실패 (형식, 범위, null/empty 등)
 * - DOM   : 도메인 규칙(invariant) 위반 (재시도해도 해결되지 않음)
 * - APP   : 유즈케이스/오케스트레이션 단계 실패 (상태/흐름 문제)
 * - INFRA : DB/네트워크 등 인프라 문제 (재시도 후보)
 * - EXT   : 외부 시스템(S3, 외부 API 등) 연동 문제 (재시도 후보)
 * <p>
 * 4) message 규칙
 * - 사용자/운영 로그에 노출 가능한 "안전한 메시지"만 사용
 * - 이메일/휴대폰 등 PII(민감정보)는 절대 포함하지 않는다.
 * - 상세 원인은 로그의 exception stacktrace / context(MDC)로 추적한다.
 * <p>
 * 5) enum 항목 주석 규칙(필수)
 * - 각 항목 위에 아래 정보를 주석으로 명시한다.
 * - 발생 위치(예: Batch Step, Domain Method, Adapter)
 * - 트리거 조건(어떤 입력/상태에서 발생하는지)
 * - 운영 의도(재시도 의미 여부, 스킵/중단/격리(DLT) 권장)
 */
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    /**
     * =====================================
     * VAL
     * =====================================
     */
    OAUTH_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "AUT-VAL-001", ErrorCategory.VAL, "OAuth 요청이 올바르지 않습니다."),
    OAUTH_USER_INFO_INVALID(HttpStatus.BAD_REQUEST, "AUT-VAL-002", ErrorCategory.VAL,"OAuth 사용자 정보가 유효하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUT-VAL-003", ErrorCategory.VAL, "인증에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUT-VAL-004", ErrorCategory.VAL, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUT-VAL-005", ErrorCategory.VAL, "폐기된 리프레시 토큰입니다."),
    REFRESH_TOKEN_OWNER_MISMATCH(HttpStatus.UNAUTHORIZED, "AUT-VAL-006", ErrorCategory.VAL, "토큰 소유자가 일치하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUT-VAL-007", ErrorCategory.VAL, "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUT-VAL-008", ErrorCategory.VAL, "리프레시 토큰이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUT-VAL-009", ErrorCategory.VAL, "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUT-VAL-010", ErrorCategory.VAL, "토큰이 만료되었습니다."),
    OAUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUT-VAL-011", ErrorCategory.VAL, "OAuth 인증에 실패했습니다."),
    INVALID_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "AUT-VAL-012", ErrorCategory.VAL, "로그인 요청이 올바르지 않습니다."),
    INVALID_BIRTH_DATE(HttpStatus.BAD_REQUEST, "AUT-VAL-013", ErrorCategory.VAL, "생년월일 값이 올바르지 않습니다."),
    INVALID_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "AUT-VAL-014", ErrorCategory.VAL, "회원 상태값이 올바르지 않습니다."),


    /**
     * =====================================
     * DOM
     * =====================================
     */
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AUT-DOM-001",ErrorCategory.DOM, "이미 사용 중인 이메일입니다."),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "AUT-DOM-002",ErrorCategory.DOM, "이미 사용 중인 전화번호입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUT-DOM-003", ErrorCategory.DOM, "회원을 찾을 수 없습니다."),
    MOBILE_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUT-DOM-004", ErrorCategory.DOM, "초기 할당할 모바일 요금제를 찾을 수 없습니다."),
    FORBIDDEN_MEMBER_STATUS(HttpStatus.FORBIDDEN, "AUT-DOM-005", ErrorCategory.DOM, "현재 회원 상태로는 요청을 처리할 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final ErrorCategory category;
    private final String message;


    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public String message() {
        return message;
    }
}
