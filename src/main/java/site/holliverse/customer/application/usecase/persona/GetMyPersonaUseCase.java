package site.holliverse.customer.application.usecase.persona;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.IndexPersonaSnapshot;
import site.holliverse.customer.persistence.entity.IndexTscoreSnapshot;
import site.holliverse.customer.persistence.entity.PersonaType;
import site.holliverse.customer.persistence.repository.IndexPersonaSnapshotRepository;
import site.holliverse.customer.persistence.repository.IndexTscoreSnapshotRepository;
import site.holliverse.customer.persistence.repository.PersonaTypeRepository;
import site.holliverse.customer.error.CustomerErrorCode;
import site.holliverse.customer.error.CustomerException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 로그인 사용자 기준으로 "현재 페르소나 + T-Score 6지수"를 조회한다.
 *
 * 동작 순서:
 * 1) 최신 T-Score snapshot 조회
 * 2) 최신 Persona snapshot 조회
 * 3) snapshot 이 가리키는 persona_type 조회
 * 4) 위 정보가 없으면 fallback persona 사용
 *
 * fallback 정책:
 * - persona_type 은 Repository 쿼리 우선순위(현재 SPACE_EXPLORER 우선)를 따른다.
 * - T-Score snapshot 이 없으면 6개 지수를 모두 50으로 내려준다.
 */
@Service
@Profile("customer")
public class GetMyPersonaUseCase {

    private static final BigDecimal DEFAULT_TSCORE = BigDecimal.valueOf(50);

    private final IndexPersonaSnapshotRepository indexPersonaSnapshotRepository;
    private final IndexTscoreSnapshotRepository indexTscoreSnapshotRepository;
    private final PersonaTypeRepository personaTypeRepository;

    public GetMyPersonaUseCase(
            IndexPersonaSnapshotRepository indexPersonaSnapshotRepository,
            IndexTscoreSnapshotRepository indexTscoreSnapshotRepository,
            PersonaTypeRepository personaTypeRepository
    ) {
        this.indexPersonaSnapshotRepository = indexPersonaSnapshotRepository;
        this.indexTscoreSnapshotRepository = indexTscoreSnapshotRepository;
        this.personaTypeRepository = personaTypeRepository;
    }

    @Transactional(readOnly = true)
    public PersonaDetailResult execute(Long memberId) {
        // 1) 최신 T-Score 조회
        // - 데이터가 있으면 실제 점수를 사용
        // - 없으면 snapshotDate=null + 6개 지수=50 기본값 구성
        PersonaDetailResult.TscoreIndex tscoreIndex = indexTscoreSnapshotRepository
                .findTopByMemberIdOrderBySnapshotDateDesc(memberId)
                .map(this::toTscoreIndex)
                .orElseGet(() -> new PersonaDetailResult.TscoreIndex(
                        null,
                        DEFAULT_TSCORE,
                        DEFAULT_TSCORE,
                        DEFAULT_TSCORE,
                        DEFAULT_TSCORE,
                        DEFAULT_TSCORE,
                        DEFAULT_TSCORE
                ));

        // 2) 최신 Persona snapshot 조회
        // snapshot 에서 persona_type_id 를 읽어 실제 persona_type 을 찾는다.
        Optional<IndexPersonaSnapshot> latestPersona = indexPersonaSnapshotRepository
                .findTopByMemberIdOrderBySnapshotDateDesc(memberId);

        if (latestPersona.isPresent()) {
            Optional<PersonaType> personaType = personaTypeRepository.findById(latestPersona.get().getPersonaTypeId());
            if (personaType.isPresent()) {
                // 3) 정상 경로: snapshot + persona_type 모두 존재
                return toResult(personaType.get(), tscoreIndex, false);
            }
        }

        // 4) fallback 경로
        // - 신규 회원(스냅샷 없음)
        // - 스냅샷은 있는데 persona_type row가 없는 경우
        PersonaType fallback = personaTypeRepository.findDefaultFallback()
                .orElseThrow(() -> new CustomerException(CustomerErrorCode.PERSONA_TYPE_NOT_FOUND));
        return toResult(fallback, tscoreIndex, true);
    }

    private PersonaDetailResult.TscoreIndex toTscoreIndex(IndexTscoreSnapshot row) {
        // Entity -> Result 변환만 담당 (비즈니스 분기 없음)
        return new PersonaDetailResult.TscoreIndex(
                row.getSnapshotDate(),
                row.getExploreTscore(),
                row.getBenefitTrendTscore(),
                row.getMultiDeviceTscore(),
                row.getFamilyHomeTscore(),
                row.getInternetSecurityTscore(),
                row.getStabilityTscore()
        );
    }

    private PersonaDetailResult toResult(PersonaType row, PersonaDetailResult.TscoreIndex tscoreIndex, boolean isDefault) {
        // Entity -> Result 변환 + fallback 여부 플래그 주입
        return new PersonaDetailResult(
                row.getPersonaTypeId(),
                row.getCharacterName(),
                row.getShortDesc(),
                row.getCharacterDescription(),
                row.getVersion(),
                row.getIsActive(),
                toTagList(row.getTags()),
                tscoreIndex,
                isDefault
        );
    }

    private List<String> toTagList(String[] tags) {
        // DB 배열 컬럼(tags)이 null이어도 API 응답은 빈 리스트로 통일
        if (tags == null) {
            return List.of();
        }
        return Arrays.asList(tags);
    }
}
