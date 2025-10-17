package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.order.OrderDtos;
import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.OrderStatus;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.RoleRepository;
import net.awords.agriecombackend.repository.ShopOrderRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ShopOrderService shopOrderService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopAdminService shopAdminService;
    @Autowired
    private ShopProductService shopProductService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ShopOrderRepository shopOrderRepository;

    private User buyer;
    private User merchantA;
    private User merchantB;
    private Long appleId;
    private Long bananaId;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            return roleRepository.save(r);
        });

        buyer = new User();
        buyer.setUsername("order-buyer");
        buyer.setPassword("pwd");
        buyer.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(buyer);

        merchantA = createMerchant("merchant-a", userRole);
        merchantB = createMerchant("merchant-b", userRole);

    createShopForMerchant(merchantA, "绿色农庄");
    createShopForMerchant(merchantB, "阳光果蔬");

        appleId = createProduct(merchantA.getUsername(), "有机苹果", new BigDecimal("10.00"), 50, "fruits", "Europe");
        bananaId = createProduct(merchantB.getUsername(), "生态香蕉", new BigDecimal("6.00"), 80, "fruits", "Asia");
    }

    @Test
    @DisplayName("下单时支持多店铺拆单")
    void createOrderSplitAcrossShops() {
        OrderDtos.CreateOrderRequest request = buildRequest();

        OrderDtos.OrderDetail detail = orderService.create(buyer.getUsername(), request);

        assertThat(detail.shopOrders).hasSize(2);
        assertThat(detail.totalAmount).isEqualByComparingTo("26.00");
        assertThat(detail.status).isEqualTo(OrderStatus.CREATED.name());

        Product apple = productRepository.findById(appleId).orElseThrow();
        Product banana = productRepository.findById(bananaId).orElseThrow();
        assertThat(apple.getStock()).isEqualTo(48);
        assertThat(banana.getStock()).isEqualTo(77);
    }

    @Test
    @DisplayName("未发货订单可取消且库存回滚")
    void cancelOrderRestoresStock() {
        OrderDtos.OrderDetail detail = orderService.create(buyer.getUsername(), buildRequest());
        Long orderId = detail.id;
        orderService.cancel(buyer.getUsername(), orderId);
        OrderDtos.OrderDetail cancelled = orderService.get(buyer.getUsername(), orderId);
        assertThat(cancelled.status).isEqualTo(OrderStatus.CANCELLED.name());
        assertThat(cancelled.shopOrders).allMatch(s -> s.status.equals(OrderStatus.CANCELLED.name()));

        Product apple = productRepository.findById(appleId).orElseThrow();
        Product banana = productRepository.findById(bananaId).orElseThrow();
        assertThat(apple.getStock()).isEqualTo(50);
        assertThat(banana.getStock()).isEqualTo(80);
    }

    @Test
    @DisplayName("商户发货更新状态与销量")
    void merchantShipUpdatesStatusAndSales() {
        OrderDtos.OrderDetail detail = orderService.create(buyer.getUsername(), buildRequest());

        List<Long> shopOrderIds = shopOrderRepository.findByOrderGroupId(detail.id).stream()
                .map(o -> o.getId())
                .toList();
        assertThat(shopOrderIds).hasSize(2);

        shopOrderService.ship(merchantA.getUsername(), shopOrderIds.get(0), buildShipRequest());
        OrderDtos.OrderDetail afterFirstShip = orderService.get(buyer.getUsername(), detail.id);
        assertThat(afterFirstShip.status).isEqualTo(OrderStatus.PROCESSING.name());

        shopOrderService.ship(merchantB.getUsername(), shopOrderIds.get(1), buildShipRequest());
        OrderDtos.OrderDetail afterSecondShip = orderService.get(buyer.getUsername(), detail.id);
        assertThat(afterSecondShip.status).isEqualTo(OrderStatus.SHIPPED.name());

        Product apple = productRepository.findById(appleId).orElseThrow();
        Product banana = productRepository.findById(bananaId).orElseThrow();
        assertThat(apple.getSales()).isEqualTo(2);
        assertThat(banana.getSales()).isEqualTo(3);
    }

    private OrderDtos.CreateOrderRequest buildRequest() {
        OrderDtos.CreateOrderRequest request = new OrderDtos.CreateOrderRequest();
        request.shippingAddress = "上海市浦东新区张江路 123 号";
        OrderDtos.CreateOrderRequest.Item appleItem = new OrderDtos.CreateOrderRequest.Item();
        appleItem.productId = appleId;
        appleItem.quantity = 2;
        OrderDtos.CreateOrderRequest.Item bananaItem = new OrderDtos.CreateOrderRequest.Item();
        bananaItem.productId = bananaId;
        bananaItem.quantity = 3;
        request.items = List.of(appleItem, bananaItem);
        return request;
    }

    private OrderDtos.ShipRequest buildShipRequest() {
        OrderDtos.ShipRequest request = new OrderDtos.ShipRequest();
        request.logisticsProvider = "极兔速递";
        request.trackingNumber = "JT123456";
        return request;
    }

    private User createMerchant(String username, Role baseRole) {
        User merchant = new User();
        merchant.setUsername(username);
        merchant.setPassword("pwd");
        merchant.setRoles(new HashSet<>(Set.of(baseRole)));
        return userRepository.save(merchant);
    }

    private Long createShopForMerchant(User merchant, String name) {
        ShopDtos.CreateRequest request = new ShopDtos.CreateRequest();
        request.name = name;
        request.description = name + " 旗舰店";
        request.logoUrl = "https://cdn.example.com/" + name + ".png";
        shopService.applyForShop(merchant.getUsername(), request);
        Shop shop = shopRepository.findByOwnerId(merchant.getId()).orElseThrow();
        shopAdminService.approve(shop.getId());
        return shop.getId();
    }

    private Long createProduct(String username, String name, BigDecimal price, int stock, String category, String origin) {
        ProductDtos.CreateRequest request = new ProductDtos.CreateRequest();
        request.name = name;
        request.description = name;
        request.price = price;
        request.stock = stock;
        request.category = category;
        request.origin = origin;
        return shopProductService.create(username, request).id;
    }
}
