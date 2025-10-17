package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.order.OrderDtos;
import net.awords.agriecombackend.dto.order.OrderMapper;
import net.awords.agriecombackend.entity.OrderGroup;
import net.awords.agriecombackend.entity.OrderItem;
import net.awords.agriecombackend.entity.OrderStatus;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.ShopOrder;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.OrderGroupRepository;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.ShopOrderRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 商户侧订单服务，负责列表、详情与发货逻辑。
 */
@Service
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    public ShopOrderService(ShopOrderRepository shopOrderRepository,
                            OrderGroupRepository orderGroupRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            CacheManager cacheManager) {
        this.shopOrderRepository = shopOrderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public OrderDtos.PageResult<OrderDtos.ShopOrderSummary> list(String username, int page, int size) {
        User merchant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : Math.min(size, 50);
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);
        Page<ShopOrder> result = shopOrderRepository.findByShopOwnerId(merchant.getId(), pageable);
        return OrderMapper.toPageResult(result, result.getContent().stream().map(OrderMapper::toShopOrderSummary).toList());
    }

    @Transactional(readOnly = true)
    public OrderDtos.ShopOrderDetail get(String username, Long shopOrderId) {
        User merchant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        ShopOrder order = shopOrderRepository.findByIdAndShopOwnerId(shopOrderId, merchant.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
        return OrderMapper.toShopOrderDetail(order);
    }

    @Transactional
    public OrderDtos.ShopOrderDetail ship(String username, Long shopOrderId, OrderDtos.ShipRequest request) {
        User merchant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        ShopOrder order = shopOrderRepository.findByIdAndShopOwnerId(shopOrderId, merchant.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));

        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已取消订单无法发货");
        }
        if (OrderStatus.SHIPPED.name().equals(order.getStatus()) || OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单已发货");
        }

        order.setStatus(OrderStatus.SHIPPED.name());
        order.setLogisticsProvider(request != null ? request.logisticsProvider : null);
        order.setTrackingNumber(request != null ? request.trackingNumber : null);
        order.setShippedAt(OffsetDateTime.now());

        shopOrderRepository.save(order);
        updateOrderGroupStatus(order.getOrderGroup());
        updateProductSales(order);
        evictCaches(order);

        return OrderMapper.toShopOrderDetail(order);
    }

    private void updateProductSales(ShopOrder order) {
        if (CollectionUtils.isEmpty(order.getOrderItems())) {
            return;
        }
    List<Product> products = order.getOrderItems().stream()
        .map(OrderItem::getProduct)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                long newSales = product.getSales() == null ? 0L : product.getSales();
                product.setSales(newSales + item.getQuantity());
            }
        }
        productRepository.saveAll(products);
    }

    private void updateOrderGroupStatus(OrderGroup group) {
        if (group == null) {
            return;
        }
        boolean allCancelled = group.getShopOrders().stream()
                .allMatch(order -> OrderStatus.CANCELLED.name().equals(order.getStatus()));
        boolean allShipped = group.getShopOrders().stream()
                .allMatch(order -> OrderStatus.SHIPPED.name().equals(order.getStatus()) || OrderStatus.COMPLETED.name().equals(order.getStatus()));
        if (allCancelled) {
            group.setStatus(OrderStatus.CANCELLED.name());
        } else if (allShipped) {
            group.setStatus(OrderStatus.SHIPPED.name());
        } else {
            group.setStatus(OrderStatus.PROCESSING.name());
        }
        orderGroupRepository.save(group);
    }

    private void evictCaches(ShopOrder order) {
        Cache productCache = cacheManager.getCache("product:detail");
        Cache shopCache = cacheManager.getCache("shop:detail");
        if (shopCache != null && order.getShop() != null) {
            shopCache.evict(order.getShop().getId());
        }
        if (productCache != null) {
            order.getOrderItems().stream()
                    .map(OrderItem::getProduct)
                    .filter(Objects::nonNull)
                    .map(Product::getId)
                    .distinct()
                    .forEach(productCache::evict);
        }
    }
}
