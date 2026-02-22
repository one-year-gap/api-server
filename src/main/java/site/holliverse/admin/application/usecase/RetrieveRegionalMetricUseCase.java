package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminRegionalMetricDao;
import site.holliverse.admin.query.dao.RegionalMetricRawData;
import site.holliverse.admin.web.dto.analytics.AdminRegionalMetricRequestDto;

import java.util.List;

@Profile("admin")
@Service
@RequiredArgsConstructor
public class RetrieveRegionalMetricUseCase {

    private final AdminRegionalMetricDao adminRegionalMetricDao;

    @Transactional(readOnly = true)
    public List<RegionalMetricRawData> execute(AdminRegionalMetricRequestDto requestDto) {
        return adminRegionalMetricDao.findRegionalAverages(requestDto.yyyymm());
    }
}
