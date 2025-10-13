package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.ShopStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShopAdminServiceTest {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopAdminService shopAdminService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ShopRepository shopRepository;

    private User applicant;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        applicant = new User();
        applicant.setUsername("applicant");
        applicant.setPassword("pwd");
        applicant.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(applicant);

        ShopDtos.CreateRequest req = new ShopDtos.CreateRequest();
        req.name = "绿野生鲜";
        req.description = "主营有机蔬菜";
        req.logoUrl = "https://cdn.example.com/shop.png";
        shopService.applyForShop(applicant.getUsername(), req);
    }

    @Test
    @DisplayName("管理员默认获取待审核列表")
    void listPending() {
        List<ShopDtos.DetailResponse> list = shopAdminService.listByStatus(ShopStatus.PENDING_REVIEW);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).status).isEqualTo(ShopStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("管理员可审批通过店铺")
    void approveShop() {
        Long shopId = shopRepository.findByOwnerId(applicant.getId()).orElseThrow().getId();
        ShopDtos.DetailResponse response = shopAdminService.approve(shopId);
        assertThat(response.status).isEqualTo(ShopStatus.ACTIVE);
    }

    @Test
    @DisplayName("非待审核店铺无法重复审批")
    void approveNonPending() {
        Long shopId = shopRepository.findByOwnerId(applicant.getId()).orElseThrow().getId();
        shopAdminService.approve(shopId);
        assertThatThrownBy(() -> shopAdminService.approve(shopId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("仅待审核店铺可审批通过");
    }

    @Test
    @DisplayName("管理员可驳回店铺申请")
    void rejectShop() {
        Long shopId = shopRepository.findByOwnerId(applicant.getId()).orElseThrow().getId();
        ShopDtos.DetailResponse response = shopAdminService.reject(shopId);
        assertThat(response.status).isEqualTo(ShopStatus.REJECTED);
    }

    @Test
    @DisplayName("管理员可封禁已上线店铺")
    void suspendShop() {
        Long shopId = shopRepository.findByOwnerId(applicant.getId()).orElseThrow().getId();
        shopAdminService.approve(shopId);
        ShopDtos.DetailResponse response = shopAdminService.suspend(shopId);
        assertThat(response.status).isEqualTo(ShopStatus.SUSPENDED);
    }

    @Test
    @DisplayName("未上线店铺无法封禁")
    void suspendNonActive() {
        Long shopId = shopRepository.findByOwnerId(applicant.getId()).orElseThrow().getId();
        assertThatThrownBy(() -> shopAdminService.suspend(shopId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("仅已上线店铺可封禁");
    }
}
