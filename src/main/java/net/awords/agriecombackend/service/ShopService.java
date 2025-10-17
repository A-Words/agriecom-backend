package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.dto.shop.ShopMapper;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.RoleRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import net.awords.agriecombackend.repository.UserRepository;
import net.awords.agriecombackend.security.RoleConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;

    public ShopService(ShopRepository shopRepository, UserRepository userRepository, RoleRepository roleRepository, ProductRepository productRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ShopDtos.DetailResponse applyForShop(String username, ShopDtos.CreateRequest request) {
        User owner = loadUser(username);
        assertUserWithoutShop(owner);

        Shop shop = new Shop();
        shop.setName(request.name);
        shop.setDescription(request.description);
        shop.setLogoUrl(request.logoUrl);
        shop.setOwner(owner);
        shop.setStatus(ShopStatus.PENDING_REVIEW);

        shopRepository.save(shop);
        ensureMerchantRole(owner);

        return ShopMapper.toDetailResponse(shop, 0L);
    }

    @Transactional(readOnly = true)
    public ShopDtos.DetailResponse getMyShop(String username) {
        User owner = loadUser(username);
        Shop shop = shopRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "尚未创建店铺"));
        long productCount = productRepository.countByShopId(shop.getId());
        return ShopMapper.toDetailResponse(shop, productCount);
    }

    @Transactional
    public ShopDtos.DetailResponse updateMyShop(String username, ShopDtos.UpdateRequest request) {
        User owner = loadUser(username);
        Shop shop = shopRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "尚未创建店铺"));

        shop.setName(request.name);
        shop.setDescription(request.description);
        shop.setLogoUrl(request.logoUrl);

        shopRepository.save(shop);

        long productCount = productRepository.countByShopId(shop.getId());
        return ShopMapper.toDetailResponse(shop, productCount);
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "当前用户不存在"));
    }

    private void assertUserWithoutShop(User owner) {
        Optional<Shop> existing = shopRepository.findByOwnerId(owner.getId());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已经拥有店铺，无需重复申请");
        }
    }

    private void ensureMerchantRole(User owner) {
        boolean alreadyMerchant = owner.getRoles().stream().anyMatch(r -> RoleConstants.MERCHANT.equalsIgnoreCase(r.getName()));
        if (alreadyMerchant) {
            return;
        }
        Role role = roleRepository.findByName(RoleConstants.MERCHANT)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleConstants.MERCHANT);
                    return roleRepository.save(newRole);
                });
        owner.getRoles().add(role);
        userRepository.save(owner);
    }
}
