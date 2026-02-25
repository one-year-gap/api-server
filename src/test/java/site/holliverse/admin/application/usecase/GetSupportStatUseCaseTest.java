package site.holliverse.admin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.admin.query.dao.AdminSupportStatDao;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetSupportStatUseCaseTest {

    @InjectMocks
    private GetSupportStatUseCase getSupportStatUseCase;

    @Mock
    private AdminSupportStatDao adminSupportStatDao;

    @Test
    @DisplayName("상담 처리 현황 통계를 성공적으로 조회한다.")
    void execute_Success() {
        // given: Dao가 반환할 가짜(Mock) 데이터를 준비
        AdminSupportStatRawData mockData = new AdminSupportStatRawData(10L, 4L, 3L, 3L);
        given(adminSupportStatDao.getSupportStatusStats()).willReturn(mockData);

        // when: UseCase를 실행
        AdminSupportStatRawData result = getSupportStatUseCase.execute();

        // then: 결과가 가짜 데이터와 일치하는지 검증하고, Dao가 1번 호출되었는지 확인
        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.openCount()).isEqualTo(4L);
        assertThat(result.supportingCount()).isEqualTo(3L);
        assertThat(result.closedCount()).isEqualTo(3L);

        verify(adminSupportStatDao).getSupportStatusStats();
    }
}