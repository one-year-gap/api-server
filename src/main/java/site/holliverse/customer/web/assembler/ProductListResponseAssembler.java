package site.holliverse.customer.web.assembler;

import site.holliverse.customer.application.usecase.product.ProductListResult;
import site.holliverse.customer.web.dto.PageMeta;
import site.holliverse.customer.web.dto.product.ProductContent;
import site.holliverse.customer.web.dto.product.ProductDetailResponse;
import site.holliverse.customer.web.dto.product.ProductListResponse;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductListResponseAssembler {

    private final ProductResponseMapper mapper;

    public ProductListResponseAssembler(ProductResponseMapper mapper) {
        this.mapper = mapper;
    }

    public ProductListResponse assemble(ProductListResult result) {
        Map<Long, ProductContent> contentByProductId = buildContentMap(result);
        Set<Long> bestIdsSet = new HashSet<>(result.bestProductIds());
        List<ProductDetailResponse> content = result.products().getContent().stream()
                .map(p -> mapper.toDetailResponse(p, contentByProductId.get(p.productId()), bestIdsSet.contains(p.productId())))
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
                .map(m -> Map.entry(m.productId(), (ProductContent) mapper.toMobileContent(m)));
        Stream<Map.Entry<Long, ProductContent>> internet = result.internets().stream()
                .map(i -> Map.entry(i.productId(), (ProductContent) mapper.toInternetContent(i)));
        Stream<Map.Entry<Long, ProductContent>> iptv = result.iptvs().stream()
                .map(i -> Map.entry(i.productId(), (ProductContent) mapper.toIptvContent(i)));
        Stream<Map.Entry<Long, ProductContent>> addon = result.addons().stream()
                .map(a -> Map.entry(a.productId(), (ProductContent) mapper.toAddonContent(a)));
        Stream<Map.Entry<Long, ProductContent>> tabWatch = result.tabWatchPlans().stream()
                .map(t -> Map.entry(t.productId(), (ProductContent) mapper.toTabWatchContent(t)));
        return Stream.of(mobile, internet, iptv, addon, tabWatch)
                .flatMap(s -> s)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
