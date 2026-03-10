package site.holliverse.customer.application.usecase.persona;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.holliverse.customer.persistence.entity.IndexPersonaSnapshot;
import site.holliverse.customer.persistence.entity.IndexTscoreSnapshot;
import site.holliverse.customer.persistence.entity.PersonaType;
import site.holliverse.customer.persistence.repository.IndexPersonaSnapshotRepository;
import site.holliverse.customer.persistence.repository.IndexTscoreSnapshotRepository;
import site.holliverse.customer.persistence.repository.PersonaTypeRepository;
import site.holliverse.shared.error.CustomException;
import site.holliverse.shared.error.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetMyPersonaUseCase 분기 로직 검증 테스트.
 *
 * 검증 포인트:
 * - 정상 조회 경로
 * - fallback 경로
 * - 예외 경로
 */
@ExtendWith(MockitoExtension.class)
class GetMyPersonaUseCaseTest {

    @Mock
    private IndexPersonaSnapshotRepository indexPersonaSnapshotRepository;
    @Mock
    private IndexTscoreSnapshotRepository indexTscoreSnapshotRepository;
    @Mock
    private PersonaTypeRepository personaTypeRepository;

    @InjectMocks
    private GetMyPersonaUseCase getMyPersonaUseCase;

    @Test
    @DisplayName("snapshot and persona_type exist then return member persona with tscore")
    void returnsAssignedPersonaWithLatestTscore() {
        // given: tscore snapshot + persona snapshot + persona_type 모두 존재
        Long memberId = 100L;
        LocalDate snapshotDate = LocalDate.of(2026, 3, 10);

        when(indexTscoreSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.of(tscoreSnapshot(memberId, snapshotDate)));
        when(indexPersonaSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.of(personaSnapshot(memberId, snapshotDate, 10L)));
        when(personaTypeRepository.findById(10L))
                .thenReturn(Optional.of(personaType(10L, "ANALYST", new String[]{"analysis", "sharp"})));

        // when: 유스케이스 실행
        PersonaDetailResult result = getMyPersonaUseCase.execute(memberId);

        // then: 저장된 실제 persona_type/tscore를 그대로 반환하고 fallback은 타지 않는다.
        assertThat(result.personaTypeId()).isEqualTo(10L);
        assertThat(result.characterName()).isEqualTo("ANALYST");
        assertThat(result.tags()).containsExactly("analysis", "sharp");
        assertThat(result.isDefault()).isFalse();
        assertThat(result.tscoreIndex().snapshotDate()).isEqualTo(snapshotDate);
        assertThat(result.tscoreIndex().exploreTscore()).isEqualByComparingTo("61");
        assertThat(result.tscoreIndex().benefitTrendTscore()).isEqualByComparingTo("59");
        assertThat(result.tscoreIndex().multiDeviceTscore()).isEqualByComparingTo("54");
        assertThat(result.tscoreIndex().familyHomeTscore()).isEqualByComparingTo("48");
        assertThat(result.tscoreIndex().internetSecurityTscore()).isEqualByComparingTo("52");
        assertThat(result.tscoreIndex().stabilityTscore()).isEqualByComparingTo("57");
        verify(personaTypeRepository, never()).findDefaultFallback();
    }

    @Test
    @DisplayName("no snapshots then return fallback persona and default tscore 50")
    void returnsFallbackPersonaAndDefaultTscore() {
        // given: 신규 회원처럼 스냅샷 데이터가 전혀 없는 상태
        Long memberId = 200L;
        when(indexTscoreSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.empty());
        when(indexPersonaSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.empty());
        when(personaTypeRepository.findDefaultFallback())
                .thenReturn(Optional.of(personaType(99L, "SPACE_EXPLORER", new String[]{"starter"})));

        // when
        PersonaDetailResult result = getMyPersonaUseCase.execute(memberId);

        // then: fallback persona + tscore 6개 기본값(50) 반환
        assertThat(result.personaTypeId()).isEqualTo(99L);
        assertThat(result.characterName()).isEqualTo("SPACE_EXPLORER");
        assertThat(result.isDefault()).isTrue();
        assertThat(result.tscoreIndex().snapshotDate()).isNull();
        assertThat(result.tscoreIndex().exploreTscore()).isEqualByComparingTo("50");
        assertThat(result.tscoreIndex().benefitTrendTscore()).isEqualByComparingTo("50");
        assertThat(result.tscoreIndex().multiDeviceTscore()).isEqualByComparingTo("50");
        assertThat(result.tscoreIndex().familyHomeTscore()).isEqualByComparingTo("50");
        assertThat(result.tscoreIndex().internetSecurityTscore()).isEqualByComparingTo("50");
        assertThat(result.tscoreIndex().stabilityTscore()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("snapshot exists but persona_type missing then fallback")
    void fallsBackWhenPersonaTypeMissing() {
        // given: persona snapshot은 있으나, 해당 persona_type row가 유실된 상태
        Long memberId = 300L;
        LocalDate snapshotDate = LocalDate.of(2026, 3, 10);

        when(indexTscoreSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.of(tscoreSnapshot(memberId, snapshotDate)));
        when(indexPersonaSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.of(personaSnapshot(memberId, snapshotDate, 777L)));
        when(personaTypeRepository.findById(777L)).thenReturn(Optional.empty());
        when(personaTypeRepository.findDefaultFallback())
                .thenReturn(Optional.of(personaType(1L, "SPACE_EXPLORER", new String[]{"starter"})));

        // when
        PersonaDetailResult result = getMyPersonaUseCase.execute(memberId);

        // then: snapshot persona는 포기하고 fallback persona로 내려준다.
        assertThat(result.personaTypeId()).isEqualTo(1L);
        assertThat(result.characterName()).isEqualTo("SPACE_EXPLORER");
        assertThat(result.isDefault()).isTrue();
        verify(personaTypeRepository).findById(777L);
        verify(personaTypeRepository).findDefaultFallback();
    }

    @Test
    @DisplayName("fallback missing then throw NOT_FOUND personaType")
    void throwsWhenFallbackMissing() {
        // given: 스냅샷도 없고 fallback persona_type도 없는 비정상 상태
        Long memberId = 400L;
        when(indexTscoreSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.empty());
        when(indexPersonaSnapshotRepository.findTopByMemberIdOrderBySnapshotDateDesc(memberId))
                .thenReturn(Optional.empty());
        when(personaTypeRepository.findDefaultFallback()).thenReturn(Optional.empty());

        // when/then: NOT_FOUND + field=personaType 예외를 확인
        assertThatThrownBy(() -> getMyPersonaUseCase.execute(memberId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customException = (CustomException) ex;
                    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(customException.getField()).isEqualTo("personaType");
                });
    }

    /**
     * persona snapshot 테스트 픽스처.
     * persona_type_id를 케이스마다 바꿔서 정상/유실 시나리오를 만든다.
     */
    private IndexPersonaSnapshot personaSnapshot(Long memberId, LocalDate snapshotDate, Long personaTypeId) {
        return IndexPersonaSnapshot.builder()
                .memberId(memberId)
                .snapshotDate(snapshotDate)
                .personaTypeId(personaTypeId)
                .sourceIndexCode("EXPLORE")
                .sourceTscore(BigDecimal.valueOf(61))
                .build();
    }

    /**
     * tscore snapshot 테스트 픽스처.
     * 실제 값이 그대로 매핑되는지 검증하기 위해 서로 다른 숫자로 세팅한다.
     */
    private IndexTscoreSnapshot tscoreSnapshot(Long memberId, LocalDate snapshotDate) {
        return IndexTscoreSnapshot.builder()
                .memberId(memberId)
                .snapshotDate(snapshotDate)
                .exploreTscore(BigDecimal.valueOf(61))
                .benefitTrendTscore(BigDecimal.valueOf(59))
                .multiDeviceTscore(BigDecimal.valueOf(54))
                .familyHomeTscore(BigDecimal.valueOf(48))
                .internetSecurityTscore(BigDecimal.valueOf(52))
                .stabilityTscore(BigDecimal.valueOf(57))
                .build();
    }

    /**
     * persona_type 테스트 픽스처.
     */
    private PersonaType personaType(Long id, String characterName, String[] tags) {
        return PersonaType.builder()
                .personaTypeId(id)
                .characterName(characterName)
                .shortDesc("desc")
                .characterDescription("character description")
                .version(1)
                .isActive(true)
                .tags(tags)
                .build();
    }
}
