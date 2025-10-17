package net.awords.agriecombackend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.awords.agriecombackend.dto.user.UserDtos;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.RoleRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setup() {
        Role role = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            return roleRepository.save(r);
        });
        user = new User();
        user.setUsername("profile-user");
        user.setPassword("secret");
        user.setRoles(new HashSet<>(Set.of(role)));
        userRepository.save(user);
    }

    @Test
    @DisplayName("获取并更新用户资料")
    void updateProfile() {
        UserDtos.ProfileResponse profile = userProfileService.getProfile(user.getUsername());
        assertThat(profile.username).isEqualTo("profile-user");
        assertThat(profile.nickname).isEqualTo("profile-user");

        UserDtos.UpdateProfileRequest request = new UserDtos.UpdateProfileRequest();
        request.nickname = "新昵称";
        request.avatarUrl = "https://cdn.example.com/avatar.png";
        request.phone = "13800000000";
        request.bio = "爱好种植";

        UserDtos.ProfileResponse updated = userProfileService.updateProfile(user.getUsername(), request);
        assertThat(updated.nickname).isEqualTo("新昵称");
        assertThat(updated.avatarUrl).isEqualTo("https://cdn.example.com/avatar.png");
        assertThat(updated.phone).isEqualTo("13800000000");
        assertThat(updated.bio).isEqualTo("爱好种植");
    }

    @Test
    @DisplayName("新增收货地址并返回列表")
    void createAddress() {
        UserDtos.CreateAddressRequest req = new UserDtos.CreateAddressRequest();
        req.recipientName = "张三";
        req.phone = "13900000000";
        req.province = "浙江省";
        req.city = "杭州市";
        req.district = "西湖区";
        req.street = "翠苑街道99号";
        req.postalCode = "310000";
        req.isDefault = true;

        UserDtos.AddressResponse address = userProfileService.createAddress(user.getUsername(), req);
        assertThat(address.recipientName).isEqualTo("张三");
        assertThat(address.isDefault).isTrue();

        List<UserDtos.AddressResponse> addresses = userProfileService.listAddresses(user.getUsername());
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).recipientName).isEqualTo("张三");
    }
}
