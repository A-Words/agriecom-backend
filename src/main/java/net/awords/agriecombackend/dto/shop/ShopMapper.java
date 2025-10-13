package net.awords.agriecombackend.dto.shop;

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
}
