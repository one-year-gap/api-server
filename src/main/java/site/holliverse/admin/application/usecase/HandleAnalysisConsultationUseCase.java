package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.domain.model.churn.ChurnEvaluationResult;
import site.holliverse.admin.domain.model.churn.feature.MemberDissatisfactionFeature;
import site.holliverse.admin.web.dto.counsel.CounselAnalysisStatus;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class HandleAnalysisConsultationUseCase {
    private final ConsultationAnalysisPersistenceService persistenceService;
    private final MemberDissatisfactionAssembler assembler;
    private final CalculateChurnScoreService calculateChurnScoreService;

    @Transactional
    public ChurnEvaluationResult execute(AnalysisResponseCommand command) {
        // 입력 검증
        validate(command);

        // 분석 결과 저장
        persistenceService.save(command);

        // 상담 feature 생성
        MemberDissatisfactionFeature dissatisfactionFeature = assembler.assemble(command);

        // 이탈률 계산
        return calculateChurnScoreService.calculateAndStore(
                command,
                resolveBaseDate(command),
                dissatisfactionFeature
        );
    }

    private void validate(AnalysisResponseCommand command) {
        // 회원 확인
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }

        // 상태 확인
        if (!CounselAnalysisStatus.COMPLETED.name().equals(command.status())) {
            throw new IllegalArgumentException("완료된 상담 분석 결과만 처리할 수 있습니다. status=" + command.status());
        }
    }

    private LocalDate resolveBaseDate(AnalysisResponseCommand command) {
        // 기준일 추출
        if (command.producedAt() != null) {
            return command.producedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDate();
        }

        // 현재 일자
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }
}
