package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.dto.product.ProductMapper;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 商户管理自己店铺下商品的核心业务逻辑，确保租户隔离。
 */
@Service
public class ShopProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    public ShopProductService(ProductRepository productRepository, ShopRepository shopRepository, UserRepository userRepository,
                              CacheManager cacheManager) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public List<ProductDtos.Detail> list(String username) {
        Shop shop = requireActiveShop(username);
        return productRepository.findAllByShopIdOrderByPublishedAtDesc(shop.getId()).stream()
                .map(ProductMapper::toDetail)
                .toList();
    }

    @Transactional
    public ProductDtos.Detail create(String username, ProductDtos.CreateRequest request) {
        Shop shop = requireActiveShop(username);
        Product product = new Product();
        product.setName(request.name);
        product.setDescription(request.description);
        product.setPrice(request.price);
        product.setStock(request.stock);
        product.setCategory(request.category);
        product.setOrigin(request.origin);
        product.setShop(shop);
        productRepository.save(product);
        evictCaches(shop.getId(), product.getId());
        return ProductMapper.toDetail(product);
    }

    @Transactional(readOnly = true)
    public ProductDtos.Detail get(String username, Long productId) {
        Product product = findOwnedProduct(username, productId);
        return ProductMapper.toDetail(product);
    }

    @Transactional
    public ProductDtos.Detail update(String username, Long productId, ProductDtos.UpdateRequest request) {
        Product product = findOwnedProduct(username, productId);
        product.setName(request.name);
        product.setDescription(request.description);
        product.setPrice(request.price);
        product.setStock(request.stock);
        product.setCategory(request.category);
        product.setOrigin(request.origin);
        productRepository.save(product);
        evictCaches(product.getShop().getId(), product.getId());
        return ProductMapper.toDetail(product);
    }

    @Transactional
    public void delete(String username, Long productId) {
        Product product = findOwnedProduct(username, productId);
        productRepository.delete(product);
        evictCaches(product.getShop().getId(), product.getId());
    }

    private Shop requireActiveShop(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "当前用户不存在"));
        return shopRepository.findByOwnerId(user.getId())
                .filter(shop -> shop.getStatus() == ShopStatus.ACTIVE || shop.getStatus() == ShopStatus.PENDING_REVIEW)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完成店铺审核"));
    }

    private Product findOwnedProduct(String username, Long productId) {
        Shop shop = requireActiveShop(username);
        return productRepository.findByIdAndShopId(productId, shop.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在或不属于您的店铺"));
    }

    private void evictCaches(Long shopId, Long productId) {
        Cache productCache = cacheManager.getCache("product:detail");
        if (productCache != null && productId != null) {
            productCache.evict(productId);
        }
        Cache shopCache = cacheManager.getCache("shop:detail");
        if (shopCache != null && shopId != null) {
            shopCache.evict(shopId);
        }
    }
}
