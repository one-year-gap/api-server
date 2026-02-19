package site.holliverse.admin.web.assembler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import site.holliverse.admin.query.dao.MemberRawData;
import site.holliverse.admin.web.dto.member.AdminMemberDto;
import site.holliverse.admin.web.dto.member.AdminMemberListResponseDto;
import site.holliverse.shared.util.DecryptionTool;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("admin")
@ExtendWith(MockitoExtension.class)
class AdminMemberAssemblerTest {

    @Mock
    private DecryptionTool decryptionTool; // 가짜 복호화 도구

    @InjectMocks
    private AdminMemberAssembler adminMemberAssembler; // 테스트 대상 (가짜 도구가 주입됨)

    @Test
    @DisplayName("암호화된 데이터를 복호화하고 마스킹(*) 처리하여 DTO로 변환한다.")
    void toListResponse_success() {
        // given
        // 1. 가짜 데이터 준비 (DB에서 가져온 상황 가정)
        MemberRawData rawData = new MemberRawData();
        rawData.setId(1L);
        rawData.setEncryptedName("ENC_NAME_VALUE");   // 암호화된 이름
        rawData.setEncryptedPhone("ENC_PHONE_VALUE"); // 암호화된 번호
        rawData.setEmail("test@holliverse.site");
        rawData.setBirthDate(LocalDate.of(2000, 1, 1));
        rawData.setMembership("VIP");
        rawData.setGender("M");
        rawData.setPlanName("Basic Plan");

        // 2. Mock 행동 정의
        // 누가 'ENC_NAME_VALUE'를 복호화해달라고 하면 '김영현'을 줘라
        given(decryptionTool.decrypt("ENC_NAME_VALUE")).willReturn("김영현");
        // 누가 'ENC_PHONE_VALUE'를 복호화해달라고 하면 '01012345678'을 줘라
        given(decryptionTool.decrypt("ENC_PHONE_VALUE")).willReturn("01012345678");

        // when
        // 3. 테스트 대상 메서드 실행 (데이터 1개, 전체 150개, 1페이지, 10개씩)
        AdminMemberListResponseDto result = adminMemberAssembler.toListResponse(
                List.of(rawData), 150, 1, 10
        );

        // then
        // 4. 검증 시작
        assertThat(result.getMembers()).hasSize(1);

        AdminMemberDto memberDto = result.getMembers().get(0);

        // [1] 이름 마스킹 확인 (김영현 -> 김*현)
        assertThat(memberDto.name()).isEqualTo("김*현");

        // [2] 전화번호 포맷팅 & 마스킹 확인 (01012345678 -> 010-****-5678)
        assertThat(memberDto.phone()).isEqualTo("010-****-5678");

        // [3] 나머지 데이터는 그대로 잘 들어갔는지
        assertThat(memberDto.email()).isEqualTo("test@holliverse.site");
        assertThat(memberDto.id()).isEqualTo(1L);

        // [4] 페이징 계산 (DTO of 메서드 로직) 확인
        // 150개 / 10개씩 = 15페이지
        assertThat(result.getPagination().getTotalPage()).isEqualTo(15);
        assertThat(result.getPagination().getTotalCount()).isEqualTo(150);
    }

    @Test
    @DisplayName("이름이 2글자일 때와 전화번호에 하이픈이 있을 때도 마스킹이 정상 동작해야 한다.")
    void masking_edge_case_test() {
        // given
        MemberRawData rawData = new MemberRawData();
        rawData.setEncryptedName("ENC_SHORT_NAME");
        rawData.setEncryptedPhone("ENC_HYPHEN_PHONE");

        // Mocking: 2글자 이름, 하이픈 있는 전화번호
        given(decryptionTool.decrypt("ENC_SHORT_NAME")).willReturn("이산");
        given(decryptionTool.decrypt("ENC_HYPHEN_PHONE")).willReturn("010-9876-5432");

        // when
        AdminMemberListResponseDto result = adminMemberAssembler.toListResponse(
                List.of(rawData), 1, 1, 10
        );

        // then
        AdminMemberDto memberDto = result.getMembers().get(0);

        // 이산 -> 이* (2글자 마스킹 규칙 확인)
        assertThat(memberDto.name()).isEqualTo("이*");

        // 010-9876-5432 -> 010-****-5432 (하이픈 제거 후 포맷팅 확인)
        assertThat(memberDto.phone()).isEqualTo("010-****-5432");
    }
}