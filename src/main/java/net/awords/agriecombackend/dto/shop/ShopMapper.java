package net.awords.agriecombackend.dto.shop;

import net.awords.agriecombackend.dto.shop.ShopDtos.ProductSummary;
import net.awords.agriecombackend.dto.shop.ShopDtos.PublicDetail;
import net.awords.agriecombackend.dto.shop.ShopDtos.PublicSummary;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Shop;

public final class ShopMapper {

    private ShopMapper() {
    }

    public static ShopDtos.DetailResponse toDetailResponse(Shop shop, long productCount) {
        ShopDtos.DetailResponse detail = new ShopDtos.DetailResponse();
        detail.id = shop.getId();
        detail.name = shop.getName();
        detail.description = shop.getDescription();
        detail.logoUrl = shop.getLogoUrl();
        detail.status = shop.getStatus();
        detail.createdAt = shop.getCreatedAt();
        detail.updatedAt = shop.getUpdatedAt();
        detail.productCount = productCount;
        detail.pendingOrderCount = 0L;
        detail.completedOrderCount = 0L;
        return detail;
    }

    /**
     * 将店铺信息映射为前台展示所需的摘要字段。
     */
    public static PublicSummary toPublicSummary(Shop shop, long productCount) {
        PublicSummary summary = new PublicSummary();
        summary.id = shop.getId();
        summary.name = shop.getName();
        summary.description = shop.getDescription();
        summary.logoUrl = shop.getLogoUrl();
        summary.createdAt = shop.getCreatedAt();
        summary.productCount = productCount;
        return summary;
    }

    /**
     * 构建店铺主页详情，包含商品列表。
     */
    public static PublicDetail toPublicDetail(Shop shop, long productCount, java.util.List<Product> products) {
        PublicDetail detail = new PublicDetail();
        detail.shop = toPublicSummary(shop, productCount);
        detail.products = products.stream().map(ShopMapper::toProductSummary).toList();
        return detail;
    }

    private static ProductSummary toProductSummary(Product product) {
        ProductSummary summary = new ProductSummary();
        summary.id = product.getId();
        summary.name = product.getName();
        summary.description = product.getDescription();
        summary.price = product.getPrice();
        summary.stock = product.getStock();
        return summary;
    }
}
