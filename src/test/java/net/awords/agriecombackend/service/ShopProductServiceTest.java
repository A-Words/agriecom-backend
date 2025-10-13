package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.RoleRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShopProductServiceTest {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopAdminService shopAdminService;
    @Autowired
    private ShopProductService shopProductService;
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
        merchant.setUsername("product-merchant");
        merchant.setPassword("pwd");
        merchant.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(merchant);

        ProductDtos.CreateRequest req = new ProductDtos.CreateRequest();
        req.name = "占位";
        req.description = "待替换";
        req.price = new BigDecimal("1.00");
        req.stock = 1;

        ShopDtos.CreateRequest shopReq = new ShopDtos.CreateRequest();
        shopReq.name = "花果山";
        shopReq.description = "特色水果";
        shopReq.logoUrl = "https://cdn.example.com/logo-shop-product.png";
        shopService.applyForShop(merchant.getUsername(), shopReq);
        Shop shop = shopRepository.findByOwnerId(merchant.getId()).orElseThrow();
        shopAdminService.approve(shop.getId());
        shopId = shop.getId();

        // 初始化一个商品
        shopProductService.create(merchant.getUsername(), req);
    }

    @Test
    @DisplayName("商户可列出自己店铺下的商品")
    void listOwnProducts() {
        List<ProductDtos.Detail> products = shopProductService.list(merchant.getUsername());
        assertThat(products).isNotEmpty();
    }

    @Test
    @DisplayName("商户可新增商品")
    void createProduct() {
        ProductDtos.CreateRequest request = new ProductDtos.CreateRequest();
        request.name = "苹果";
        request.description = "富士苹果";
        request.price = new BigDecimal("3.20");
        request.stock = 50;

        ProductDtos.Detail detail = shopProductService.create(merchant.getUsername(), request);
        assertThat(detail.id).isNotNull();
        assertThat(detail.name).isEqualTo("苹果");
    }

    @Nested
    class WithExistingProduct {

        private Long productId;

        @BeforeEach
        void initProduct() {
            productId = productRepository.findAll().stream()
                    .filter(p -> p.getShop().getId().equals(shopId))
                    .map(Product::getId)
                    .findFirst()
                    .orElseThrow();
        }

        @Test
        @DisplayName("商户可读取商品详情")
        void getProduct() {
            ProductDtos.Detail detail = shopProductService.get(merchant.getUsername(), productId);
            assertThat(detail.id).isEqualTo(productId);
        }

        @Test
        @DisplayName("商户可更新商品信息")
        void updateProduct() {
            ProductDtos.UpdateRequest update = new ProductDtos.UpdateRequest();
            update.name = "香蕉";
            update.description = "进口香蕉";
            update.price = new BigDecimal("2.50");
            update.stock = 60;

            ProductDtos.Detail detail = shopProductService.update(merchant.getUsername(), productId, update);
            assertThat(detail.name).isEqualTo("香蕉");
            Product persisted = productRepository.findById(productId).orElseThrow();
            assertThat(persisted.getPrice()).isEqualByComparingTo("2.50");
        }

        @Test
        @DisplayName("商户可删除商品")
        void deleteProduct() {
            shopProductService.delete(merchant.getUsername(), productId);
            assertThat(productRepository.findById(productId)).isEmpty();
        }
    }

    @Test
    @DisplayName("商户无法访问他人商品")
    void accessOthersProductShouldFail() {
        // 创建另一个商户的店铺与商品
        User other = new User();
        other.setUsername("other");
        other.setPassword("pwd");
        other.setRoles(new HashSet<>(Set.of(roleRepository.findByName("USER").orElseThrow())));
        userRepository.save(other);

        ShopDtos.CreateRequest shopReq = new ShopDtos.CreateRequest();
        shopReq.name = "另一家";
        shopReq.description = "其他商户";
        shopReq.logoUrl = null;
        shopService.applyForShop(other.getUsername(), shopReq);
        Shop otherShop = shopRepository.findByOwnerId(other.getId()).orElseThrow();
        shopAdminService.approve(otherShop.getId());

        ProductDtos.CreateRequest productReq = new ProductDtos.CreateRequest();
        productReq.name = "梨";
        productReq.description = "雪梨";
        productReq.price = new BigDecimal("4.00");
        productReq.stock = 30;
        shopProductService.create(other.getUsername(), productReq);
        Long otherProductId = productRepository.findAll().stream()
                .filter(p -> p.getShop().getId().equals(otherShop.getId()))
                .map(Product::getId)
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> shopProductService.get(merchant.getUsername(), otherProductId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("不存在或不属于您的店铺");
    }
}
