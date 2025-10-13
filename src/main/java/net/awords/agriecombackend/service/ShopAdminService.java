package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.dto.shop.ShopMapper;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShopAdminService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public ShopAdminService(ShopRepository shopRepository, ProductRepository productRepository) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ShopDtos.DetailResponse> listByStatus(ShopStatus status) {
        return shopRepository.findAllByStatusOrderByCreatedAtAsc(status).stream()
                .map(shop -> ShopMapper.toDetailResponse(shop, productRepository.countByShopId(shop.getId())))
                .toList();
    }

    @Transactional
    public ShopDtos.DetailResponse approve(Long shopId) {
        Shop shop = loadShop(shopId);
        if (shop.getStatus() != ShopStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅待审核店铺可审批通过");
        }
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);
        return ShopMapper.toDetailResponse(shop, productRepository.countByShopId(shop.getId()));
    }

    @Transactional
    public ShopDtos.DetailResponse reject(Long shopId) {
        Shop shop = loadShop(shopId);
        if (shop.getStatus() != ShopStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅待审核店铺可驳回");
        }
        shop.setStatus(ShopStatus.REJECTED);
        shopRepository.save(shop);
        return ShopMapper.toDetailResponse(shop, productRepository.countByShopId(shop.getId()));
    }

    @Transactional
    public ShopDtos.DetailResponse suspend(Long shopId) {
        Shop shop = loadShop(shopId);
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅已上线店铺可封禁");
        }
        shop.setStatus(ShopStatus.SUSPENDED);
        shopRepository.save(shop);
        return ShopMapper.toDetailResponse(shop, productRepository.countByShopId(shop.getId()));
    }

    private Shop loadShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "店铺不存在"));
    }
}
