package site.holliverse.customer.web.mapper;

import org.springframework.stereotype.Component;
import site.holliverse.customer.application.usecase.ProductDetailResult;
import site.holliverse.customer.application.usecase.ProductListResult;
import site.holliverse.customer.application.usecase.dto.AddonDetailDto;
import site.holliverse.customer.application.usecase.dto.InternetDetailDto;
import site.holliverse.customer.application.usecase.dto.IptvDetailDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.dto.TabWatchPlanDetailDto;
import site.holliverse.customer.web.dto.AddonContent;
import site.holliverse.customer.web.dto.InternetContent;
import site.holliverse.customer.web.dto.IptvContent;
import site.holliverse.customer.web.dto.MobileContent;
import site.holliverse.customer.web.dto.PageMeta;
import site.holliverse.customer.web.dto.ProductContent;
import site.holliverse.customer.web.dto.ProductDetailResponse;
import site.holliverse.customer.web.dto.ProductListResponse;
import site.holliverse.customer.web.dto.TabWatchContent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductResponseMapper {

    public ProductListResponse toListResponse(ProductListResult result) {
        Map<Long, ProductContent> contentByProductId = buildContentMap(result);
        List<ProductDetailResponse> content = result.products().getContent().stream()
                .map(p -> toDetailResponse(p, contentByProductId.get(p.productId())))
                .toList();
        PageMeta page = new PageMeta(
                result.products().getTotalElements(),
                result.products().getTotalPages(),
                result.products().getNumber(),
                result.products().getSize()
        );
        return new ProductListResponse(page, content);
    }

    private Map<Long, ProductContent> buildContentMap(ProductListResult result) {
        Stream<Map.Entry<Long, ProductContent>> mobile = result.mobilePlans().stream()
                .map(m -> Map.entry(m.productId(), (ProductContent) toMobileContent(m)));
        Stream<Map.Entry<Long, ProductContent>> internet = result.internets().stream()
                .map(i -> Map.entry(i.productId(), (ProductContent) toInternetContent(i)));
        Stream<Map.Entry<Long, ProductContent>> iptv = result.iptvs().stream()
                .map(i -> Map.entry(i.productId(), (ProductContent) toIptvContent(i)));
        Stream<Map.Entry<Long, ProductContent>> addon = result.addons().stream()
                .map(a -> Map.entry(a.productId(), (ProductContent) toAddonContent(a)));
        Stream<Map.Entry<Long, ProductContent>> tabWatch = result.tabWatchPlans().stream()
                .map(t -> Map.entry(t.productId(), (ProductContent) toTabWatchContent(t)));
        return Stream.of(mobile, internet, iptv, addon, tabWatch)
                .flatMap(s -> s)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private ProductDetailResponse toDetailResponse(ProductSummaryDto p, ProductContent content) {
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

    private MobileContent toMobileContent(MobilePlanDetailDto m) {
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

    private InternetContent toInternetContent(InternetDetailDto i) {
        return new InternetContent(i.speedMbps(), i.addonBenefit());
    }

    private IptvContent toIptvContent(IptvDetailDto i) {
        return new IptvContent(i.channelCount(), i.addonBenefit());
    }

    private AddonContent toAddonContent(AddonDetailDto a) {
        return new AddonContent(a.addonType().name(), a.description());
    }

    private TabWatchContent toTabWatchContent(TabWatchPlanDetailDto t) {
        return new TabWatchContent(t.dataAmount(), t.benefitVoiceCall(), t.benefitSms());
    }
}
