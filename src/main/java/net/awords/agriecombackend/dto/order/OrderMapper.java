package net.awords.agriecombackend.dto.order;

import net.awords.agriecombackend.dto.order.OrderDtos.OrderDetail;
import net.awords.agriecombackend.dto.order.OrderDtos.OrderItem;
import net.awords.agriecombackend.dto.order.OrderDtos.OrderSummary;
import net.awords.agriecombackend.dto.order.OrderDtos.PageResult;
import net.awords.agriecombackend.dto.order.OrderDtos.ShopOrderDetail;
import net.awords.agriecombackend.dto.order.OrderDtos.ShopOrderSummary;
import net.awords.agriecombackend.entity.OrderGroup;
import net.awords.agriecombackend.entity.ShopOrder;
import net.awords.agriecombackend.entity.User;
import org.springframework.data.domain.Page;

/**
 * 订单领域的 DTO 转换工具。
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderSummary toOrderSummary(OrderGroup group) {
        OrderSummary summary = new OrderSummary();
        summary.id = group.getId();
        summary.status = group.getStatus();
        summary.totalAmount = group.getTotalAmount();
        summary.createdAt = group.getCreatedAt();
        summary.updatedAt = group.getUpdatedAt();
        summary.shippingAddress = group.getShippingAddress();
        summary.shopOrders = group.getShopOrders().stream().map(OrderMapper::toShopOrderSummary).toList();
        return summary;
    }

    public static OrderDetail toOrderDetail(OrderGroup group) {
        OrderDetail detail = new OrderDetail();
        User buyer = group.getBuyer();
        detail.id = group.getId();
        detail.status = group.getStatus();
        detail.totalAmount = group.getTotalAmount();
        detail.createdAt = group.getCreatedAt();
        detail.updatedAt = group.getUpdatedAt();
        detail.shippingAddress = group.getShippingAddress();
        detail.buyerId = buyer != null ? buyer.getId() : null;
        detail.buyerUsername = buyer != null ? buyer.getUsername() : null;
        detail.shopOrders = group.getShopOrders().stream().map(OrderMapper::toShopOrderSummary).toList();
        return detail;
    }

    public static ShopOrderSummary toShopOrderSummary(ShopOrder order) {
        ShopOrderSummary summary = new ShopOrderSummary();
        summary.id = order.getId();
        summary.shopId = order.getShop() != null ? order.getShop().getId() : null;
        summary.shopName = order.getShop() != null ? order.getShop().getName() : null;
        summary.status = order.getStatus();
        summary.totalAmount = order.getTotalAmount();
        summary.createdAt = order.getCreatedAt();
        summary.shippingAddress = order.getShippingAddress();
        return summary;
    }

    public static ShopOrderDetail toShopOrderDetail(ShopOrder order) {
        ShopOrderDetail detail = new ShopOrderDetail();
        detail.id = order.getId();
        detail.shopId = order.getShop() != null ? order.getShop().getId() : null;
        detail.shopName = order.getShop() != null ? order.getShop().getName() : null;
        detail.status = order.getStatus();
        detail.totalAmount = order.getTotalAmount();
        detail.createdAt = order.getCreatedAt();
        detail.shippingAddress = order.getShippingAddress();
        detail.updatedAt = order.getUpdatedAt();
        detail.logisticsProvider = order.getLogisticsProvider();
        detail.trackingNumber = order.getTrackingNumber();
        detail.shippedAt = order.getShippedAt();
    detail.items = order.getOrderItems().stream()
        .map(item -> OrderMapper.toOrderItem(item))
        .toList();
        return detail;
    }

    private static OrderItem toOrderItem(net.awords.agriecombackend.entity.OrderItem entity) {
        OrderItem item = new OrderItem();
        item.id = entity.getId();
        item.productId = entity.getProduct() != null ? entity.getProduct().getId() : null;
        item.productName = entity.getProductName();
        item.price = entity.getProductSnapshotPrice();
        item.quantity = entity.getQuantity();
        item.subtotal = entity.getSubtotal();
        return item;
    }

    public static <T> PageResult<T> toPageResult(Page<?> page, java.util.List<T> items) {
        PageResult<T> result = new PageResult<>();
        result.items = items;
        result.totalElements = page.getTotalElements();
        result.totalPages = page.getTotalPages();
        result.page = page.getNumber();
        result.size = page.getSize();
        return result;
    }
}
