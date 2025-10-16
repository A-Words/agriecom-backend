package net.awords.agriecombackend.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 商品相关的请求与响应 DTO，兼顾商户后台与公共前台场景。
 */
public class ProductDtos {

    public static class CreateRequest {
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 255, message = "商品名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "商品描述长度不能超过2000字符")
        public String description;

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
        public BigDecimal price;

        @NotNull(message = "库存不能为空")
        @PositiveOrZero(message = "库存不能为负数")
        public Integer stock;

        @Size(max = 100, message = "分类长度不能超过100字符")
        public String category;

        @Size(max = 255, message = "产地长度不能超过255字符")
        public String origin;
    }

    public static class UpdateRequest {
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 255, message = "商品名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "商品描述长度不能超过2000字符")
        public String description;

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
        public BigDecimal price;

        @NotNull(message = "库存不能为空")
        @PositiveOrZero(message = "库存不能为负数")
        public Integer stock;

        @Size(max = 100, message = "分类长度不能超过100字符")
        public String category;

        @Size(max = 255, message = "产地长度不能超过255字符")
        public String origin;
    }

    /**
     * 商户管理后台查看商品详情时使用。
     */
    public static class Detail {
        public Long id;
        public String name;
        public String description;
        public BigDecimal price;
        public Integer stock;
        public String category;
        public String origin;
        public Long sales;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
        public OffsetDateTime publishedAt;
        public Long shopId;
        public String shopName;
    }

    /**
     * 面向公共前台的商品摘要信息，兼容分页、排序与筛选展示。
     */
    public static class PublicSummary {
        public Long id;
        public String name;
        public String description;
        public BigDecimal price;
        public String category;
        public String origin;
        public Long sales;
        public OffsetDateTime publishedAt;
        public Long shopId;
        public String shopName;
    }

    /**
     * 通用分页响应包装，提供基本的分页元信息。
     */
    public static class PageResult<T> {
        public List<T> items;
        public long totalElements;
        public int totalPages;
        public int page;
        public int size;
    }
}
