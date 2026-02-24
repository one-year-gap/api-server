package site.holliverse.admin.web.mapper;

import org.mapstruct.Mapper;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;
import site.holliverse.admin.web.dto.support.AdminSupportStatResponseDto;

@Mapper(componentModel = "spring") // 스프링 빈으로 등록되어 Controller에서 의존성 주입 가능
public interface AdminSupportStatMapper {

    // MapStruct가 자동으로 1:1 매핑 코드를 생성해줌
    AdminSupportStatResponseDto toResponseDto(AdminSupportStatRawData rowData);
}