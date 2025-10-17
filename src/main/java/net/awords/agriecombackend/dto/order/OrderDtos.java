package net.awords.agriecombackend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 订单相关 DTO，覆盖买家下单、订单查询以及商户发货等场景。
 */
public class OrderDtos {

    public static class CreateOrderRequest {
        @NotEmpty(message = "请至少选择一个商品")
        public List<Item> items;

        @Size(max = 512, message = "收货地址长度不能超过512字符")
        public String shippingAddress;

        public static class Item {
            @NotNull(message = "商品 ID 不能为空")
            public Long productId;

            @NotNull(message = "购买数量不能为空")
            @Min(value = 1, message = "购买数量至少为 1")
            public Integer quantity;
        }
    }

    public static class OrderSummary {
        public Long id;
        public String status;
        public BigDecimal totalAmount;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
        public String shippingAddress;
        public List<ShopOrderSummary> shopOrders;
    }

    public static class OrderDetail extends OrderSummary {
        public Long buyerId;
        public String buyerUsername;
    }

    public static class ShopOrderSummary {
        public Long id;
        public Long shopId;
        public String shopName;
        public String status;
        public BigDecimal totalAmount;
        public OffsetDateTime createdAt;
        public String shippingAddress;
    }

    public static class ShopOrderDetail extends ShopOrderSummary {
        public OffsetDateTime updatedAt;
        public String logisticsProvider;
        public String trackingNumber;
        public OffsetDateTime shippedAt;
        public List<OrderItem> items;
    }

    public static class OrderItem {
        public Long id;
        public Long productId;
        public String productName;
        public BigDecimal price;
        public Integer quantity;
        public BigDecimal subtotal;
    }

    public static class ShipRequest {
        @Size(max = 128, message = "物流公司名称过长")
        public String logisticsProvider;

        @Size(max = 128, message = "运单号长度不能超过128字符")
        public String trackingNumber;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long totalElements;
        public int totalPages;
        public int page;
        public int size;
    }
}
