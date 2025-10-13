package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.ProductRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShopPublicServiceTest {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopAdminService shopAdminService;
    @Autowired
    private ShopPublicService shopPublicService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ProductRepository productRepository;

    private User merchant;
    private Long shopId;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        merchant = new User();
        merchant.setUsername("public-merchant");
        merchant.setPassword("pwd");
        merchant.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(merchant);

        ShopDtos.CreateRequest req = new ShopDtos.CreateRequest();
        req.name = "田园优选";
        req.description = "主打绿色蔬菜";
        req.logoUrl = "https://cdn.example.com/logo-public.png";
        shopService.applyForShop(merchant.getUsername(), req);

        Shop shop = shopRepository.findByOwnerId(merchant.getId()).orElseThrow();
        shopAdminService.approve(shop.getId());
        shopId = shop.getId();

        Product p1 = new Product();
        p1.setName("西红柿");
        p1.setDescription("新鲜采摘");
        p1.setPrice(new BigDecimal("3.50"));
        p1.setStock(100);
        p1.setShop(shop);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("黄瓜");
        p2.setDescription("无公害");
        p2.setPrice(new BigDecimal("2.20"));
        p2.setStock(80);
        p2.setShop(shop);
        productRepository.save(p2);
    }

    @Test
    @DisplayName("买家列表默认只展示上线店铺")
    void listActiveShops() {
        ShopDtos.PublicPage page = shopPublicService.list(0, 10, null);
        assertThat(page.items).hasSize(1);
        assertThat(page.totalElements).isEqualTo(1);
        assertThat(page.items.get(0).name).isEqualTo("田园优选");
        assertThat(page.items.get(0).productCount).isEqualTo(2);
    }

    @Test
    @DisplayName("买家可通过关键字过滤店铺")
    void searchByKeyword() {
        ShopDtos.PublicPage matchPage = shopPublicService.list(0, 10, "田园");
        ShopDtos.PublicPage emptyPage = shopPublicService.list(0, 10, "不存在");
        assertThat(matchPage.items).hasSize(1);
        assertThat(emptyPage.items).isEmpty();
    }

    @Test
    @DisplayName("买家查看店铺主页包含商品")
    void getPublicDetail() {
        ShopDtos.PublicDetail detail = shopPublicService.getPublicDetail(shopId);
        assertThat(detail.shop.name).isEqualTo("田园优选");
        assertThat(detail.products).hasSize(2);
        assertThat(detail.products.get(0).price).isNotNull();
    }

    @Test
    @DisplayName("未上线店铺无法被浏览")
    void getNonActiveShop() {
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        shop.setStatus(ShopStatus.SUSPENDED);
        assertThatThrownBy(() -> shopPublicService.getPublicDetail(shopId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("不存在或未上线");
    }
}
