package site.holliverse.admin.web.dto.member;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 회원 목록 조회 요청 DTO
 * - 검색어, 필터링 조건, 페이징 정보를 담는다.
 */
public record AdminMemberListRequestDto(
        Integer page,
        Integer size,

        // 검색어 (이름 or 전화번호)
        String keyword,

        // 필터 조건들
        List<String> memberships,      // 등급 다중 선택
        String gender,            // 성별
        List<String> planNames,   // 요금제명 다중 선택

        // 10이 들어오면 10~19세, 20이 들어오면 20~29세를 의미
        List<Integer> ageGroups,  // 연령대 다중 선택 (예: 10, 20, 30...)

        LocalDate joinDateStart,  // 가입일 검색 시작
        LocalDate joinDateEnd     // 가입일 검색 끝
) {
    // 생성자에서 null 처리 및 기본값 설정
    public AdminMemberListRequestDto {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
    }

    // offset 계산용 편의 메서드
    public int getOffset() {
        return (page - 1) * size;
    }
}