package site.holliverse.admin.web.assembler;

import org.springframework.stereotype.Component;
import site.holliverse.admin.query.dao.dto.MonthlyStatDto;
import site.holliverse.admin.web.dto.member.MonthlyMemberStatResponseDto;

import java.util.List;

@Component
public class MemberStatisticsAssembler {

    public List<MonthlyMemberStatResponseDto> toResponseList(List<MonthlyStatDto> dtos) {
        return dtos.stream()
                .map(dto -> new MonthlyMemberStatResponseDto(
                        dto.yearMonth(),
                        dto.joinedCount(),
                        dto.leftCount()
                ))
                .toList();
    }
}