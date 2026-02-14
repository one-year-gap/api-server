package site.holliverse.customer.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.application.usecase.dto.AddonDetailDto;
import site.holliverse.customer.application.usecase.dto.InternetDetailDto;
import site.holliverse.customer.application.usecase.dto.IptvDetailDto;
import site.holliverse.customer.application.usecase.dto.MobilePlanDetailDto;
import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.application.usecase.dto.TabWatchPlanDetailDto;
import site.holliverse.customer.persistence.entity.Addon;
import site.holliverse.customer.persistence.entity.Internet;
import site.holliverse.customer.persistence.entity.Iptv;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.ProductType;
import site.holliverse.customer.persistence.entity.TabWatchPlan;
import site.holliverse.customer.persistence.repository.AddonRepository;
import site.holliverse.customer.persistence.repository.InternetRepository;
import site.holliverse.customer.persistence.repository.IptvRepository;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.TabWatchPlanRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetProductListUseCase {

    private static final Map<String, ProductType> CATEGORY_TO_TYPE = Map.of(
            "mobile", ProductType.MOBILE_PLAN,
            "internet", ProductType.INTERNET,
            "iptv", ProductType.IPTV,
            "add-on", ProductType.ADDON,
            "tab-watch", ProductType.TAB_WATCH_PLAN
    );

    private final ProductRepository productRepository;
    private final MobilePlanRepository mobilePlanRepository;
    private final InternetRepository internetRepository;
    private final IptvRepository iptvRepository;
    private final AddonRepository addonRepository;
    private final TabWatchPlanRepository tabWatchPlanRepository;

    public GetProductListUseCase(ProductRepository productRepository,
                              MobilePlanRepository mobilePlanRepository,
                              InternetRepository internetRepository,
                              IptvRepository iptvRepository,
                              AddonRepository addonRepository,
                              TabWatchPlanRepository tabWatchPlanRepository) {
        this.productRepository = productRepository;
        this.mobilePlanRepository = mobilePlanRepository;
        this.internetRepository = internetRepository;
        this.iptvRepository = iptvRepository;
        this.addonRepository = addonRepository;
        this.tabWatchPlanRepository = tabWatchPlanRepository;
    }

    @Transactional(readOnly = true)
    public ProductListResult execute(String category, int page, int size) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        String key = category.strip().toLowerCase();
        ProductType productType = CATEGORY_TO_TYPE.get(key);
        if (productType == null) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + category);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByProductType(productType, pageable);

        List<Long> productIds = products.getContent().isEmpty()
                ? List.of()
                : products.getContent().stream().map(Product::getProductId).collect(Collectors.toList());

        List<MobilePlan> mobilePlans = productType == ProductType.MOBILE_PLAN && !productIds.isEmpty()
                ? mobilePlanRepository.findByProductIdIn(productIds) : List.of();
        List<Internet> internets = productType == ProductType.INTERNET && !productIds.isEmpty()
                ? internetRepository.findByProductIdIn(productIds) : List.of();
        List<Iptv> iptvs = productType == ProductType.IPTV && !productIds.isEmpty()
                ? iptvRepository.findByProductIdIn(productIds) : List.of();
        List<Addon> addons = productType == ProductType.ADDON && !productIds.isEmpty()
                ? addonRepository.findByProductIdIn(productIds) : List.of();
        List<TabWatchPlan> tabWatchPlans = productType == ProductType.TAB_WATCH_PLAN && !productIds.isEmpty()
                ? tabWatchPlanRepository.findByProductIdIn(productIds) : List.of();

        Page<ProductSummaryDto> productDtos = products.map(this::toSummaryDto);
        return new ProductListResult(
                productDtos,
                mobilePlans.stream().map(this::toMobilePlanDto).toList(),
                internets.stream().map(this::toInternetDto).toList(),
                iptvs.stream().map(this::toIptvDto).toList(),
                addons.stream().map(this::toAddonDto).toList(),
                tabWatchPlans.stream().map(this::toTabWatchPlanDto).toList()
        );
    }

    private ProductSummaryDto toSummaryDto(Product p) {
        return new ProductSummaryDto(
                p.getProductId(),
                p.getName(),
                p.getPrice(),
                p.getSalePrice(),
                p.getProductType(),
                p.getProductCode(),
                p.getDiscountType()
        );
    }

    private MobilePlanDetailDto toMobilePlanDto(MobilePlan m) {
        return new MobilePlanDetailDto(
                m.getProductId(),
                m.getDataAmount(),
                m.getTetheringSharingData(),
                m.getBenefitBrands(),
                m.getBenefitVoiceCall(),
                m.getBenefitSms(),
                m.getBenefitMedia(),
                m.getBenefitPremium(),
                m.getBenefitSignatureFamilyDiscount()
        );
    }

    private InternetDetailDto toInternetDto(Internet i) {
        return new InternetDetailDto(i.getProductId(), i.getSpeedMbps(), i.getAddonBenefit());
    }

    private IptvDetailDto toIptvDto(Iptv i) {
        return new IptvDetailDto(i.getProductId(), i.getChannelCount(), i.getAddonBenefit());
    }

    private AddonDetailDto toAddonDto(Addon a) {
        return new AddonDetailDto(a.getProductId(), a.getAddonType(), a.getDescription());
    }

    private TabWatchPlanDetailDto toTabWatchPlanDto(TabWatchPlan t) {
        return new TabWatchPlanDetailDto(t.getProductId(), t.getDataAmount(), t.getBenefitVoiceCall(), t.getBenefitSms());
    }
}
