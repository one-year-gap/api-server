package site.holliverse.customer.web.dto.product;

public sealed interface ProductContent
        permits MobileContent, InternetContent, IptvContent, AddonContent, TabWatchContent {
}
