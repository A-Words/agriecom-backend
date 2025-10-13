package net.awords.agriecombackend.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.awords.agriecombackend.entity.ShopStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class ShopDtos {
    public static class CreateRequest {
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255, message = "店铺名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "店铺介绍长度不能超过2000字符")
        public String description;

        @Size(max = 512, message = "Logo 链接长度不能超过512字符")
        public String logoUrl;
    }

    public static class UpdateRequest {
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255, message = "店铺名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "店铺介绍长度不能超过2000字符")
        public String description;

        @Size(max = 512, message = "Logo 链接长度不能超过512字符")
        public String logoUrl;
    }

    public static class DetailResponse {
        public Long id;
        public String name;
        public String description;
        public String logoUrl;
        public ShopStatus status;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
        public long productCount;
        public long pendingOrderCount;
        public long completedOrderCount;
    }

    /**
     * 买家浏览时使用的精简店铺摘要。
     */
    public static class PublicSummary {
        public Long id;
        public String name;
        public String description;
        public String logoUrl;
        public OffsetDateTime createdAt;
        public long productCount;
    }

    /**
     * 买家查看店铺主页时返回的详细信息，包含商品列表。
     */
    public static class PublicDetail {
        public PublicSummary shop;
        public List<ProductSummary> products;
    }

    /**
     * 店铺内商品的公开展示信息。
     */
    public static class ProductSummary {
        public Long id;
        public String name;
        public String description;
        public BigDecimal price;
        public Integer stock;
    }

    /**
     * 分页包装结构，给前端提供页码与总量信息。
     */
    public static class PublicPage {
        public List<PublicSummary> items;
        public long totalElements;
        public int totalPages;
        public int page;
        public int size;
    }
}
