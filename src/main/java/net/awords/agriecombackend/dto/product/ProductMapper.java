package net.awords.agriecombackend.dto.product;

import net.awords.agriecombackend.entity.Product;

/**
 * 商品实体与 DTO 之间的转换工具。
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDtos.Detail toDetail(Product product) {
        ProductDtos.Detail detail = new ProductDtos.Detail();
        detail.id = product.getId();
        detail.name = product.getName();
        detail.description = product.getDescription();
        detail.price = product.getPrice();
        detail.stock = product.getStock();
        detail.createdAt = product.getCreatedAt();
        detail.updatedAt = product.getUpdatedAt();
        return detail;
    }
}
