package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.order.OrderDtos;
import net.awords.agriecombackend.dto.order.OrderMapper;
import net.awords.agriecombackend.entity.OrderGroup;
import net.awords.agriecombackend.entity.OrderItem;
import net.awords.agriecombackend.entity.OrderStatus;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopOrder;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.OrderGroupRepository;
import net.awords.agriecombackend.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 买家侧订单服务：负责下单、查询与取消。
 */
@Service
public class OrderService {

    private final OrderGroupRepository orderGroupRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    public OrderService(OrderGroupRepository orderGroupRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        CacheManager cacheManager) {
        this.orderGroupRepository = orderGroupRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public OrderDtos.OrderDetail create(String username, OrderDtos.CreateOrderRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.items)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单商品不能为空");
        }
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        Map<Long, Integer> quantityByProduct = new HashMap<>();
        request.items.forEach(item -> {
            if (item.productId == null || item.quantity == null || item.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品或数量非法");
            }
            quantityByProduct.merge(item.productId, item.quantity, Integer::sum);
        });

        List<Product> products = productRepository.findAllById(quantityByProduct.keySet());
        if (products.size() != quantityByProduct.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "存在无效商品");
        }

        Map<Long, Shop> shopCache = new HashMap<>();
        Map<Long, List<Product>> productsByShop = new HashMap<>();
        for (Product product : products) {
            Shop shop = product.getShop();
            if (shop == null || (shop.getStatus() != ShopStatus.ACTIVE && shop.getStatus() != ShopStatus.PENDING_REVIEW)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品所属店铺不可用");
            }
            if (product.getStock() < quantityByProduct.get(product.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存不足: " + product.getName());
            }
            shopCache.putIfAbsent(shop.getId(), shop);
            productsByShop.computeIfAbsent(shop.getId(), k -> new ArrayList<>()).add(product);
        }

        OrderGroup orderGroup = new OrderGroup();
        orderGroup.setBuyer(buyer);
        orderGroup.setStatus(OrderStatus.CREATED.name());
        orderGroup.setShippingAddress(request.shippingAddress);

        BigDecimal groupTotal = BigDecimal.ZERO;
        List<ShopOrder> allShopOrders = new ArrayList<>();

        for (Map.Entry<Long, List<Product>> entry : productsByShop.entrySet()) {
            Long shopId = entry.getKey();
            Shop shop = shopCache.get(shopId);
            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setOrderGroup(orderGroup);
            shopOrder.setShop(shop);
            shopOrder.setStatus(OrderStatus.CREATED.name());
            shopOrder.setShippingAddress(request.shippingAddress);

            BigDecimal shopTotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            for (Product product : entry.getValue()) {
                int quantity = quantityByProduct.get(product.getId());
                OrderItem orderItem = new OrderItem();
                orderItem.setShopOrder(shopOrder);
                orderItem.setProduct(product);
                orderItem.setProductName(product.getName());
                orderItem.setProductSnapshotPrice(product.getPrice());
                orderItem.setQuantity(quantity);
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                orderItem.setSubtotal(subtotal);
                orderItems.add(orderItem);
                shopTotal = shopTotal.add(subtotal);

                product.setStock(product.getStock() - quantity);
            }
            shopOrder.setOrderItems(orderItems);
            shopOrder.setTotalAmount(shopTotal);
            groupTotal = groupTotal.add(shopTotal);
            orderGroup.getShopOrders().add(shopOrder);
            allShopOrders.add(shopOrder);
        }
        orderGroup.setTotalAmount(groupTotal);

        OrderGroup saved = orderGroupRepository.save(orderGroup);
        productRepository.saveAll(products);

        evictCachesAfterOrderChange(allShopOrders);

        return OrderMapper.toOrderDetail(saved);
    }

    @Transactional(readOnly = true)
    public OrderDtos.PageResult<OrderDtos.OrderSummary> list(String username, int page, int size) {
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : Math.min(size, 50);
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);
        Page<OrderGroup> result = orderGroupRepository.findByBuyerId(buyer.getId(), pageable);
        return OrderMapper.toPageResult(result, result.getContent().stream().map(OrderMapper::toOrderSummary).toList());
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderDetail get(String username, Long orderGroupId) {
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        OrderGroup group = orderGroupRepository.findByIdAndBuyerId(orderGroupId, buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
        return OrderMapper.toOrderDetail(group);
    }

    @Transactional
    public OrderDtos.OrderDetail cancel(String username, Long orderGroupId) {
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        OrderGroup group = orderGroupRepository.findByIdAndBuyerId(orderGroupId, buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));

        if (OrderStatus.CANCELLED.name().equals(group.getStatus())) {
            return OrderMapper.toOrderDetail(group);
        }

        boolean anyShipped = group.getShopOrders().stream()
                .anyMatch(order -> OrderStatus.SHIPPED.name().equals(order.getStatus()) || OrderStatus.COMPLETED.name().equals(order.getStatus()));
        if (anyShipped) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单已发货，无法取消");
        }

        group.setStatus(OrderStatus.CANCELLED.name());
        for (ShopOrder shopOrder : group.getShopOrders()) {
            shopOrder.setStatus(OrderStatus.CANCELLED.name());
            for (OrderItem item : shopOrder.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                }
            }
        }
        productRepository.saveAll(extractProducts(group));
        OrderGroup saved = orderGroupRepository.save(group);
        evictCachesAfterOrderChange(group.getShopOrders());
        return OrderMapper.toOrderDetail(saved);
    }

    private List<Product> extractProducts(OrderGroup group) {
        List<Product> products = new ArrayList<>();
        for (ShopOrder shopOrder : group.getShopOrders()) {
            for (OrderItem item : shopOrder.getOrderItems()) {
                if (item.getProduct() != null) {
                    products.add(item.getProduct());
                }
            }
        }
        return products;
    }

    private void evictCachesAfterOrderChange(List<ShopOrder> shopOrders) {
        Cache productCache = cacheManager.getCache("product:detail");
        Cache shopCache = cacheManager.getCache("shop:detail");
        if (CollectionUtils.isEmpty(shopOrders)) {
            return;
        }
        for (ShopOrder order : shopOrders) {
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
}
