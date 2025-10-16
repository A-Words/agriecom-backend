package net.awords.agriecombackend.dto.product;

import net.awords.agriecombackend.dto.product.ProductDtos.PageResult;
import net.awords.agriecombackend.dto.product.ProductDtos.PublicSummary;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Shop;
import org.springframework.data.domain.Page;

import java.util.function.Function;

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
        detail.category = product.getCategory();
        detail.origin = product.getOrigin();
        detail.sales = product.getSales();
        detail.createdAt = product.getCreatedAt();
        detail.updatedAt = product.getUpdatedAt();
        detail.publishedAt = product.getPublishedAt();
        Shop shop = product.getShop();
        if (shop != null) {
            detail.shopId = shop.getId();
            detail.shopName = shop.getName();
        }
        return detail;
    }

    public static PublicSummary toPublicSummary(Product product) {
        PublicSummary summary = new PublicSummary();
        summary.id = product.getId();
        summary.name = product.getName();
        summary.description = product.getDescription();
        summary.price = product.getPrice();
        summary.category = product.getCategory();
        summary.origin = product.getOrigin();
        summary.sales = product.getSales();
        summary.publishedAt = product.getPublishedAt();
        Shop shop = product.getShop();
        if (shop != null) {
            summary.shopId = shop.getId();
            summary.shopName = shop.getName();
        }
        return summary;
    }

    public static <T> PageResult<T> toPageResult(Page<Product> page, Function<Product, T> mapper) {
        PageResult<T> result = new PageResult<>();
        result.items = page.getContent().stream().map(mapper).toList();
        result.totalElements = page.getTotalElements();
        result.totalPages = page.getTotalPages();
        result.page = page.getNumber();
        result.size = page.getSize();
        return result;
    }
}
