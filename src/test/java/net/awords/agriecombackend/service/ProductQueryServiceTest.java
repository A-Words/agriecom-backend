package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.product.ProductDtos.CreateRequest;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductQueryServiceTest {

    @Autowired
    private ProductQueryService productQueryService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopAdminService shopAdminService;
    @Autowired
    private ShopProductService shopProductService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ShopRepository shopRepository;

    private User merchant;
    private Long shopId;
    private Long appleId;
    private Long bananaId;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        merchant = new User();
        merchant.setUsername("query-merchant");
        merchant.setPassword("pwd");
        merchant.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(merchant);

        ShopDtos.CreateRequest shopRequest = new ShopDtos.CreateRequest();
        shopRequest.name = "果园百货";
        shopRequest.description = "主营水果";
        shopRequest.logoUrl = "https://cdn.example.com/logo-query.png";
        shopService.applyForShop(merchant.getUsername(), shopRequest);
        Shop shop = shopRepository.findByOwnerId(merchant.getId()).orElseThrow();
        shopAdminService.approve(shop.getId());
        shopId = shop.getId();

        CreateRequest apple = new CreateRequest();
        apple.name = "有机苹果";
        apple.description = "产自阿尔卑斯山";
        apple.price = new BigDecimal("12.50");
        apple.stock = 120;
        apple.category = "fruits";
        apple.origin = "Europe";
        appleId = shopProductService.create(merchant.getUsername(), apple).id;

        CreateRequest banana = new CreateRequest();
        banana.name = "香甜香蕉";
        banana.description = "热带风味";
        banana.price = new BigDecimal("6.30");
        banana.stock = 80;
        banana.category = "fruits";
        banana.origin = "Asia";
        bananaId = shopProductService.create(merchant.getUsername(), banana).id;

        // 设置销量以便排序测试
        Product appleEntity = productRepository.findById(appleId).orElseThrow();
        appleEntity.setSales(200L);
        Product bananaEntity = productRepository.findById(bananaId).orElseThrow();
        bananaEntity.setSales(120L);
    }

    @Test
    @DisplayName("可按分类与价格区间筛选")
    void filterByCategoryAndPriceRange() {
        ProductQueryService.ProductQuery query = new ProductQueryService.ProductQuery(
                0,
                10,
                Sort.by(Sort.Direction.ASC, "price"),
                "fruits",
                null,
                new BigDecimal("5"),
                new BigDecimal("15"),
                null,
                null,
                true
        );
        ProductDtos.PageResult<ProductDtos.PublicSummary> page = productQueryService.list(query);
        assertThat(page.items).extracting(ps -> ps.name).containsExactly("香甜香蕉", "有机苹果");
    }

    @Test
    @DisplayName("可按关键字搜索")
    void searchByKeyword() {
        ProductQueryService.ProductQuery query = new ProductQueryService.ProductQuery(
                0,
                10,
                Sort.by(Sort.Direction.ASC, "price"),
                null,
                null,
                null,
                null,
                null,
                "阿尔卑斯",
                true
        );
        ProductDtos.PageResult<ProductDtos.PublicSummary> page = productQueryService.list(query);
        assertThat(page.items).hasSize(1);
        assertThat(page.items.get(0).name).isEqualTo("有机苹果");
    }

    @Test
    @DisplayName("可按销量降序排序")
    void sortBySales() {
        ProductQueryService.ProductQuery query = new ProductQueryService.ProductQuery(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "sales"),
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
        ProductDtos.PageResult<ProductDtos.PublicSummary> page = productQueryService.list(query);
        assertThat(page.items).extracting(ps -> ps.id).containsExactly(appleId, bananaId);
    }

    @Test
    @DisplayName("商品详情包含分类与产地信息")
    void detailContainsExtendedFields() {
        ProductDtos.Detail detail = productQueryService.detail(appleId);
        assertThat(detail.category).isEqualTo("fruits");
        assertThat(detail.origin).isEqualTo("Europe");
        assertThat(detail.shopId).isEqualTo(shopId);
    }
}
