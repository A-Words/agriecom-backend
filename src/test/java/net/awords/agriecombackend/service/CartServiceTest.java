package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.cart.CartDtos;
import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.RoleRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;
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

    private User buyer;
    private User merchantA;
    private User merchantB;
    private Long appleId;
    private Long bananaId;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            return roleRepository.save(r);
        });

        buyer = new User();
        buyer.setUsername("cart-buyer");
        buyer.setPassword("pwd");
        buyer.setRoles(new HashSet<>(Set.of(role)));
        userRepository.save(buyer);

        merchantA = createMerchant("cart-merchant-a", role);
        merchantB = createMerchant("cart-merchant-b", role);

        createShopForMerchant(merchantA, "蔬菜之家");
        createShopForMerchant(merchantB, "水果小筑");

        appleId = createProduct(merchantA.getUsername(), "有机生菜", new BigDecimal("5.50"), 40);
        bananaId = createProduct(merchantB.getUsername(), "香甜香蕉", new BigDecimal("6.20"), 60);
    }

    @Test
    @DisplayName("添加购物车按店铺分组")
    void addItemGroupByShop() {
        CartDtos.AddItemRequest addApple = new CartDtos.AddItemRequest();
        addApple.productId = appleId;
        addApple.quantity = 2;
        cartService.addItem(buyer.getUsername(), addApple);

        CartDtos.AddItemRequest addBanana = new CartDtos.AddItemRequest();
        addBanana.productId = bananaId;
        addBanana.quantity = 3;
        CartDtos.CartDetail detail = cartService.addItem(buyer.getUsername(), addBanana);

        assertThat(detail.shops).hasSize(2);
        assertThat(detail.totalItems).isEqualTo(5);
    assertThat(detail.totalAmount).isEqualByComparingTo("29.60");
    }

    @Test
    @DisplayName("更新购物车数量")
    void updateQuantity() {
        CartDtos.AddItemRequest addApple = new CartDtos.AddItemRequest();
        addApple.productId = appleId;
        addApple.quantity = 1;
        cartService.addItem(buyer.getUsername(), addApple);

        CartDtos.UpdateItemRequest update = new CartDtos.UpdateItemRequest();
        update.quantity = 5;
        CartDtos.CartDetail detail = cartService.updateItem(buyer.getUsername(), appleId, update);

        assertThat(detail.totalItems).isEqualTo(5);
        assertThat(detail.shops).hasSize(1);
        assertThat(detail.shops.get(0).items.get(0).quantity).isEqualTo(5);
    }

    @Test
    @DisplayName("删除购物车商品")
    void removeItem() {
        CartDtos.AddItemRequest addApple = new CartDtos.AddItemRequest();
        addApple.productId = appleId;
        addApple.quantity = 1;
        cartService.addItem(buyer.getUsername(), addApple);

        CartDtos.CartDetail detail = cartService.removeItem(buyer.getUsername(), appleId);
        assertThat(detail.totalItems).isEqualTo(0);
        assertThat(detail.shops).isEmpty();
    }

    @Test
    @DisplayName("清空购物车")
    void clearCart() {
        CartDtos.AddItemRequest addApple = new CartDtos.AddItemRequest();
        addApple.productId = appleId;
        addApple.quantity = 1;
        cartService.addItem(buyer.getUsername(), addApple);

        CartDtos.AddItemRequest addBanana = new CartDtos.AddItemRequest();
        addBanana.productId = bananaId;
        addBanana.quantity = 2;
        cartService.addItem(buyer.getUsername(), addBanana);

        CartDtos.CartDetail detail = cartService.clear(buyer.getUsername());
        assertThat(detail.totalItems).isEqualTo(0);
        assertThat(detail.shops).isEmpty();
    }

    private User createMerchant(String username, Role role) {
        User merchant = new User();
        merchant.setUsername(username);
        merchant.setPassword("pwd");
        merchant.setRoles(new HashSet<>(Set.of(role)));
        return userRepository.save(merchant);
    }

    private void createShopForMerchant(User merchant, String name) {
        ShopDtos.CreateRequest request = new ShopDtos.CreateRequest();
        request.name = name;
        request.description = name + " 描述";
        request.logoUrl = "https://cdn.example.com/" + name + ".png";
        shopService.applyForShop(merchant.getUsername(), request);
        Shop shop = shopRepository.findByOwnerId(merchant.getId()).orElseThrow();
        shopAdminService.approve(shop.getId());
    }

    private Long createProduct(String username, String name, BigDecimal price, int stock) {
        ProductDtos.CreateRequest request = new ProductDtos.CreateRequest();
        request.name = name;
        request.description = name;
        request.price = price;
        request.stock = stock;
        request.category = "fruits";
        request.origin = "CN";
        return shopProductService.create(username, request).id;
    }
}
