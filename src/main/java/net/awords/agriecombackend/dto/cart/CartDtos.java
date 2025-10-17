package net.awords.agriecombackend.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车相关 DTO。
 */
public class CartDtos {

    public static class AddItemRequest {
        @NotNull(message = "商品不能为空")
        public Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        public Integer quantity;
    }

    public static class UpdateItemRequest {
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        public Integer quantity;
    }

    public static class CartDetail {
        public List<ShopCart> shops;
        public int totalItems;
        public BigDecimal totalAmount;
    }

    public static class ShopCart {
        public Long shopId;
        public String shopName;
        public List<Item> items;
        public BigDecimal subtotal;
    }

    public static class Item {
        public Long productId;
        public String productName;
        public BigDecimal price;
        public Integer quantity;
        public BigDecimal subtotal;
        public Integer stock;
    }
}
