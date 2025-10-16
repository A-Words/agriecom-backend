package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.ShopStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 商品高级检索使用的 Specification 组合，便于控制多条件筛选逻辑。
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
        };
    }

    public static Specification<Product> originEquals(String origin) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(origin)) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("origin")), origin.trim().toLowerCase());
        };
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceLessThanOrEqualTo(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Product> belongsToShop(Long shopId) {
        return (root, query, cb) -> shopId == null ? cb.conjunction() : cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Product> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern),
                    cb.like(cb.lower(root.get("origin")), pattern)
            );
        };
    }

    public static Specification<Product> shopStatusEquals(ShopStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.join("shop").get("status"), status);
        };
    }
}
