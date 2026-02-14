package site.holliverse.customer.application.usecase;

import site.holliverse.customer.application.usecase.dto.AddonDetailDto;
import site.holliverse.customer.application.usecase.dto.InternetDetailDto;
import site.holliverse.customer.application.usecase.dto.IptvDetailDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.dto.TabWatchPlanDetailDto;

import java.util.Optional;

public record ProductDetailResult(
        ProductSummaryDto product,
        Optional<MobilePlanDetailDto> mobilePlan,
        Optional<InternetDetailDto> internet,
        Optional<IptvDetailDto> iptv,
        Optional<AddonDetailDto> addon,
        Optional<TabWatchPlanDetailDto> tabWatchPlan
) {}
