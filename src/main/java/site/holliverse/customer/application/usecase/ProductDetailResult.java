package site.holliverse.customer.application.usecase;

import site.holliverse.customer.persistence.entity.Addon;
import site.holliverse.customer.persistence.entity.Internet;
import site.holliverse.customer.persistence.entity.Iptv;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.TabWatchPlan;

import java.util.Optional;

public record ProductDetailResult(
        Product product,
        Optional<MobilePlan> mobilePlan,
        Optional<Internet> internet,
        Optional<Iptv> iptv,
        Optional<Addon> addon,
        Optional<TabWatchPlan> tabWatchPlan
) {}
