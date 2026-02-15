package site.holliverse.customer.web.mapper;

import site.holliverse.customer.application.usecase.ProductDetailResult;
import site.holliverse.customer.application.usecase.dto.AddonDetailDto;
import site.holliverse.customer.application.usecase.dto.InternetDetailDto;
import site.holliverse.customer.application.usecase.dto.IptvDetailDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.dto.TabWatchPlanDetailDto;
import site.holliverse.customer.web.dto.product.AddonContent;
import site.holliverse.customer.web.dto.product.InternetContent;
import site.holliverse.customer.web.dto.product.IptvContent;
import site.holliverse.customer.web.dto.product.MobileContent;
import site.holliverse.customer.web.dto.product.ProductContent;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.dto.product.TabWatchContent;

public class ProductResponseMapper {

    public ProductDetailResponse toDetailResponse(ProductSummaryDto p, ProductContent content) {
        return new ProductDetailResponse(
                p.productId(),
                p.name(),
                p.productType().name(),
                p.price(),
                p.salePrice(),
                p.discountType(),
                p.productCode(),
                content
        );
    }

    public ProductDetailResponse toDetailResponse(ProductDetailResult result) {
        ProductSummaryDto p = result.product();
        ProductContent content = switch (p.productType()) {
            case MOBILE_PLAN -> result.mobilePlan().map(this::toMobileContent).orElse(null);
            case INTERNET -> result.internet().map(this::toInternetContent).orElse(null);
            case IPTV -> result.iptv().map(this::toIptvContent).orElse(null);
            case ADDON -> result.addon().map(this::toAddonContent).orElse(null);
            case TAB_WATCH_PLAN -> result.tabWatchPlan().map(this::toTabWatchContent).orElse(null);
        };

        return toDetailResponse(p, content);
    }

    public MobileContent toMobileContent(MobilePlanDetailDto m) {
        return new MobileContent(
                m.dataAmount(),
                m.tetheringSharingData(),
                m.benefitBrands(),
                m.benefitVoiceCall(),
                m.benefitSms(),
                m.benefitMedia(),
                m.benefitPremium(),
                m.benefitSignatureFamilyDiscount()
        );
    }

    public InternetContent toInternetContent(InternetDetailDto i) {
        return new InternetContent(i.planTitle(), i.speed(), i.addonBenefit(), i.benefits());
    }

    public IptvContent toIptvContent(IptvDetailDto i) {
        return new IptvContent(i.channelCount(), i.addonBenefit());
    }

    public AddonContent toAddonContent(AddonDetailDto a) {
        return new AddonContent(a.addonType().name(), a.description());
    }

    public TabWatchContent toTabWatchContent(TabWatchPlanDetailDto t) {
        return new TabWatchContent(t.dataAmount(), t.benefitVoiceCall(), t.benefitSms());
    }
}
