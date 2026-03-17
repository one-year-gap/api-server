package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.application.usecase.dto.AnalysisResponseCommand;
import site.holliverse.admin.query.dao.ConsultationAnalysisDao;

@Service
@Profile("admin")
@RequiredArgsConstructor
public class ConsultationAnalysisPersistenceService {

    private final ConsultationAnalysisDao consultationAnalysisDao;

    @Transactional
    public void save(AnalysisResponseCommand command) {
        // 분석 영속화
        consultationAnalysisDao.save(command);
    }
}
