package site.holliverse.customer.application.usecase;

import org.springframework.data.domain.Page;
import site.holliverse.customer.application.usecase.dto.AddonDetailDto;
import site.holliverse.customer.application.usecase.dto.InternetDetailDto;
import site.holliverse.customer.application.usecase.dto.IptvDetailDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.dto.TabWatchPlanDetailDto;

import java.util.List;

public record ProductListResult(
        Page<ProductSummaryDto> products,
        List<MobilePlanDetailDto> mobilePlans,
        List<InternetDetailDto> internets,
        List<IptvDetailDto> iptvs,
        List<AddonDetailDto> addons,
        List<TabWatchPlanDetailDto> tabWatchPlans
) {}
