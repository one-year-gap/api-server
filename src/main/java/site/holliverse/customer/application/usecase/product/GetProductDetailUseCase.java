package site.holliverse.customer.application.usecase.product;

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
import site.holliverse.shared.domain.model.ProductType;
import site.holliverse.customer.persistence.entity.TabWatchPlan;
import site.holliverse.customer.persistence.repository.AddonRepository;
import site.holliverse.customer.persistence.repository.InternetRepository;
import site.holliverse.customer.persistence.repository.IptvRepository;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.TabWatchPlanRepository;

import java.util.Optional;

@Service
public class GetProductDetailUseCase {

    private final ProductRepository productRepository;
    private final MobilePlanRepository mobilePlanRepository;
    private final InternetRepository internetRepository;
    private final IptvRepository iptvRepository;
    private final AddonRepository addonRepository;
    private final TabWatchPlanRepository tabWatchPlanRepository;

    public GetProductDetailUseCase(ProductRepository productRepository,
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
    public ProductDetailResult execute(Long planId) {
        Product product = productRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다: " + planId));

        Long productId = product.getProductId();
        ProductType type = product.getProductType();

        Optional<MobilePlan> mobilePlan = Optional.empty();
        Optional<Internet> internet = Optional.empty();
        Optional<Iptv> iptv = Optional.empty();
        Optional<Addon> addon = Optional.empty();
        Optional<TabWatchPlan> tabWatchPlan = Optional.empty();

        switch (type) {
            case MOBILE_PLAN -> mobilePlan = mobilePlanRepository.findById(productId);
            case INTERNET -> internet = internetRepository.findById(productId);
            case IPTV -> iptv = iptvRepository.findById(productId);
            case ADDON -> addon = addonRepository.findById(productId);
            case TAB_WATCH_PLAN -> tabWatchPlan = tabWatchPlanRepository.findById(productId);
        }

        ProductSummaryDto productDto = toSummaryDto(product);
        return new ProductDetailResult(
                productDto,
                mobilePlan.map(this::toMobilePlanDto),
                internet.map(this::toInternetDto),
                iptv.map(this::toIptvDto),
                addon.map(this::toAddonDto),
                tabWatchPlan.map(this::toTabWatchPlanDto)
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
        return new InternetDetailDto(i.getProductId(), i.getPlanTitle(), i.getSpeed(), i.getBenefits());
    }

    private IptvDetailDto toIptvDto(Iptv i) {
        return new IptvDetailDto(i.getProductId(), i.getPlanTitle(), i.getChannelCount(), i.getBenefits());
    }

    private AddonDetailDto toAddonDto(Addon a) {
        return new AddonDetailDto(a.getProductId(), a.getAddonType(), a.getDescription());
    }

    private TabWatchPlanDetailDto toTabWatchPlanDto(TabWatchPlan t) {
        return new TabWatchPlanDetailDto(t.getProductId(), t.getDataAmount(), t.getBenefitVoiceCall(), t.getBenefitSms());
    }
}
