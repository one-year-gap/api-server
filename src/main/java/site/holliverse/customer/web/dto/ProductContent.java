package site.holliverse.customer.web.dto;

public sealed interface ProductContent
        permits MobileContent, InternetContent, IptvContent, AddonContent, TabWatchContent {
}
