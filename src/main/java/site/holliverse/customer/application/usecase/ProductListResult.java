package site.holliverse.customer.application.usecase;

import org.springframework.data.domain.Page;
import site.holliverse.customer.persistence.entity.Addon;
import site.holliverse.customer.persistence.entity.Internet;
import site.holliverse.customer.persistence.entity.Iptv;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.TabWatchPlan;

import java.util.List;

public record ProductListResult(
        Page<Product> products,
        List<MobilePlan> mobilePlans,
        List<Internet> internets,
        List<Iptv> iptvs,
        List<Addon> addons,
        List<TabWatchPlan> tabWatchPlans
) {}
