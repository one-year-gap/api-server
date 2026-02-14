package site.holliverse.customer.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        ProductType productType = CATEGORY_TO_TYPE.getOrDefault(
                category != null ? category.toLowerCase() : "",
                ProductType.MOBILE_PLAN
        );
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

        return new ProductListResult(products, mobilePlans, internets, iptvs, addons, tabWatchPlans);
    }
}
