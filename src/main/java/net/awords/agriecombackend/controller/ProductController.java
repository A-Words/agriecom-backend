package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.service.ProductQueryService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "商品相关接口")
public class ProductController {
    private static final Map<String, String> SORT_MAPPINGS = Map.of(
            "price", "price",
            "sales", "sales",
            "created_at", "createdAt",
            "createdAt", "createdAt",
            "updated_at", "updatedAt",
            "updatedAt", "updatedAt",
            "published_at", "publishedAt",
            "publishedAt", "publishedAt"
    );

    private final ProductQueryService productQueryService;

    public ProductController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @Operation(summary = "分页检索商品列表",
            description = "支持分页、排序、筛选与关键字搜索；默认仅展示已上线店铺商品。")
    @GetMapping
    public ApiResponseDTO<ProductDtos.PageResult<ProductDtos.PublicSummary>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) List<String> sortParams,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "origin", required = false) String origin,
            @RequestParam(name = "price_min", required = false) BigDecimal priceMin,
            @RequestParam(name = "price_max", required = false) BigDecimal priceMax,
            @RequestParam(name = "shop_id", required = false) Long shopId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "include_inactive_shop", defaultValue = "false") boolean includeInactiveShop
    ) {
        ProductQueryService.ProductQuery query = new ProductQueryService.ProductQuery(
                page,
                size,
                buildSort(sortParams),
                category,
                origin,
                priceMin,
                priceMax,
                shopId,
                keyword,
                !includeInactiveShop
        );
        return ApiResponseDTO.success(productQueryService.list(query));
    }

    @Operation(summary = "关键词搜索商品",
            description = "通过关键词模糊匹配商品名称、描述、分类与产地。" )
    @GetMapping("/search")
    public ApiResponseDTO<ProductDtos.PageResult<ProductDtos.PublicSummary>> search(
            @RequestParam(name = "q") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) List<String> sortParams,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "origin", required = false) String origin,
            @RequestParam(name = "price_min", required = false) BigDecimal priceMin,
            @RequestParam(name = "price_max", required = false) BigDecimal priceMax,
            @RequestParam(name = "shop_id", required = false) Long shopId
    ) {
        ProductQueryService.ProductQuery baseQuery = new ProductQueryService.ProductQuery(
                page,
                size,
                buildSort(sortParams),
                category,
                origin,
                priceMin,
                priceMax,
                shopId,
                keyword,
                true
        );
        return ApiResponseDTO.success(productQueryService.list(baseQuery));
    }

    @Operation(summary = "获取商品详情",
            description = "商品详情启用 Redis 缓存，命中后可显著降低数据库压力。")
    @GetMapping("/{productId}")
    public ApiResponseDTO<ProductDtos.Detail> detail(@PathVariable Long productId) {
        return ApiResponseDTO.success(productQueryService.detail(productId));
    }

    private Sort buildSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        }
        List<Order> orders = sortParams.stream()
                .map(param -> {
                    String[] parts = param.split(",");
                    String source = parts[0].trim();
                    String mappedProperty = SORT_MAPPINGS.get(source);
                    if (mappedProperty == null) {
                        return null;
                    }
                    Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
                    return new Order(direction, mappedProperty);
                })
                .filter(order -> order != null)
                .collect(Collectors.toList());
        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        }
        return Sort.by(orders);
    }
}
