package site.holliverse.admin.web.dto.counsel;

import site.holliverse.admin.query.dao.CounselTrafficMonthlyRawData;

import java.util.List;

public record CounselTrafficMonthResponseDto(
        List<CounselTrafficMonthlyRawData> items,
        Integer maxCount
) {
    public static CounselTrafficMonthResponseDto of(List<CounselTrafficMonthlyRawData> data) {
        int maxCount = data.stream()
                .mapToInt(CounselTrafficMonthlyRawData::count)
                .max()
                .orElse(0);

        return new CounselTrafficMonthResponseDto(data, maxCount);
    }
}
