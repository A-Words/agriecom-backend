package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.product.ProductMapper;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.ProductSpecifications;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * 商品查询服务：封装分页、排序、筛选、搜索及缓存逻辑，供商户后台与前台共用。
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductDtos.PageResult<ProductDtos.PublicSummary> list(ProductQuery query) {
        int sanitizedPage = Math.max(query.page(), 0);
        int sanitizedSize = query.size() <= 0 ? 20 : Math.min(query.size(), 100);
        Sort sort = query.sort() == null ? Sort.by(Sort.Direction.DESC, "publishedAt") : query.sort();
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize, sort);

    Specification<Product> spec = (root, q, cb) -> cb.conjunction();
    spec = spec.and(ProductSpecifications.shopStatusEquals(query.onlyActiveShop() ? ShopStatus.ACTIVE : null));
    spec = spec.and(ProductSpecifications.categoryEquals(query.category()));
    spec = spec.and(ProductSpecifications.originEquals(query.origin()));
    spec = spec.and(ProductSpecifications.priceGreaterThanOrEqualTo(query.priceMin()));
    spec = spec.and(ProductSpecifications.priceLessThanOrEqualTo(query.priceMax()));
    spec = spec.and(ProductSpecifications.belongsToShop(query.shopId()));
    spec = spec.and(ProductSpecifications.keywordLike(query.keyword()));

        Page<Product> page = productRepository.findAll(spec, pageable);
        return ProductMapper.toPageResult(page, ProductMapper::toPublicSummary);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "product:detail", key = "#productId")
    public ProductDtos.Detail detail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (product.getShop() != null && product.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品所在店铺未上线");
        }
        return ProductMapper.toDetail(product);
    }

    /**
     * 查询入参，控制分页、排序与过滤条件。
     */
    public record ProductQuery(
            int page,
            int size,
            Sort sort,
            String category,
            String origin,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Long shopId,
            String keyword,
            boolean onlyActiveShop
    ) {
        public ProductQuery withKeyword(String newKeyword) {
            return new ProductQuery(page, size, sort, category, origin, priceMin, priceMax, shopId, newKeyword, onlyActiveShop);
        }
    }
}
