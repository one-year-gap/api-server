package site.holliverse.admin.web.dto.counsel;

import site.holliverse.admin.query.dao.CounselTrafficDailyRawData;
import java.util.List;

public record CounselTrafficTDailyResponseDto(
        List<CounselTrafficDailyRawData> items ,
        Integer maxCount
) {
    public static CounselTrafficTDailyResponseDto of(List<CounselTrafficDailyRawData> data) {
        int maxCount = data.stream()
                .mapToInt(CounselTrafficDailyRawData::count)
                .max()
                .orElse(0);

        return new CounselTrafficTDailyResponseDto(data,maxCount);
    }
}
