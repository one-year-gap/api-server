package site.holliverse.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import site.holliverse.admin.query.dao.AdminMemberDao;
import site.holliverse.admin.query.jooq.enums.MemberStatusType;
import site.holliverse.admin.web.dto.member.AdminMemberBulkStatusUpdateRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"admin", "test"})
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("CI 환경에 PostgreSQL 도커가 없으므로 빌드 시 제외, 로컬에서만 수동 실행")
class AdminMemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AdminMemberDao adminMemberDao;

    @Test
    @DisplayName("DB 연동 통합테스트: 상태 일괄 변경 시 실제 DB 값이 바뀐다.")
    void bulkUpdate_IntegrationTest() throws Exception {
        // given: 도커 DB에 1번, 2번 회원이 ACTIVE 상태로 있다고 가정
        List<Long> targetIds = List.of(1L, 2L);
        AdminMemberBulkStatusUpdateRequestDto req =
                new AdminMemberBulkStatusUpdateRequestDto(targetIds, "BANNED");

        // when: API 호출
        mockMvc.perform(patch("/api/v1/admin/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // then: DB를 조회해서 값이 'BANNED'로 바뀌었는지 확인
        boolean existsBanned = adminMemberDao.existsByIdAndStatus(1L, MemberStatusType.BANNED);
        assertThat(existsBanned).isTrue();
    }
}