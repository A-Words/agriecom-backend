package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.cart.CartDtos;
import net.awords.agriecombackend.entity.CartItem;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.CartItemRepository;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 购物车业务逻辑，负责增删改查以及店铺分组。
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartDtos.CartDetail getCart(String username) {
        User user = loadUser(username);
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return toDetail(user, items);
    }

    @Transactional
    public CartDtos.CartDetail addItem(String username, CartDtos.AddItemRequest request) {
        if (request == null || request.productId == null || request.quantity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请求参数缺失");
        }
        if (request.quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于 0");
        }
        User user = loadUser(username);
        Product product = productRepository.findById(request.productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));
        validateProductForCart(product);

        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });
        int newQuantity = item.getQuantity() == null ? request.quantity : item.getQuantity() + request.quantity;
        ensureStock(product, newQuantity);
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return getCart(username);
    }

    @Transactional
    public CartDtos.CartDetail updateItem(String username, Long productId, CartDtos.UpdateItemRequest request) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不能为空");
        }
        if (request == null || request.quantity == null || request.quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于 0");
        }
        User user = loadUser(username);
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车中不存在该商品"));
        Product product = item.getProduct();
        if (product == null) {
            cartItemRepository.delete(item);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        validateProductForCart(product);
        ensureStock(product, request.quantity);
        item.setQuantity(request.quantity);
        cartItemRepository.save(item);
        return getCart(username);
    }

    @Transactional
    public CartDtos.CartDetail removeItem(String username, Long productId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不能为空");
        }
        User user = loadUser(username);
        cartItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return getCart(username);
    }

    @Transactional
    public CartDtos.CartDetail clear(String username) {
        User user = loadUser(username);
        cartItemRepository.deleteByUserId(user.getId());
        return toDetail(user, List.of());
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    private void validateProductForCart(Product product) {
        Shop shop = product.getShop();
        if (shop == null || shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不可购买");
        }
    }

    private void ensureStock(Product product, int quantity) {
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存不足");
        }
    }

    private CartDtos.CartDetail toDetail(User user, List<CartItem> items) {
        CartDtos.CartDetail detail = new CartDtos.CartDetail();
        if (CollectionUtils.isEmpty(items)) {
            detail.shops = List.of();
            detail.totalItems = 0;
            detail.totalAmount = BigDecimal.ZERO;
            return detail;
        }
        Map<Long, CartDtos.ShopCart> shops = new LinkedHashMap<>();
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItem> invalidItems = new ArrayList<>();

        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product == null || product.getShop() == null) {
                invalidItems.add(item);
                continue;
            }
            if (product.getStock() == null || product.getStock() <= 0) {
                invalidItems.add(item);
                continue;
            }
            Shop shop = product.getShop();
            CartDtos.ShopCart shopCart = shops.computeIfAbsent(shop.getId(), id -> {
                CartDtos.ShopCart sc = new CartDtos.ShopCart();
                sc.shopId = shop.getId();
                sc.shopName = shop.getName();
                sc.items = new ArrayList<>();
                sc.subtotal = BigDecimal.ZERO;
                return sc;
            });

            CartDtos.Item dto = new CartDtos.Item();
            dto.productId = product.getId();
            dto.productName = product.getName();
            dto.price = product.getPrice();
            dto.quantity = item.getQuantity();
            dto.stock = product.getStock();
            dto.subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            shopCart.items.add(dto);
            shopCart.subtotal = shopCart.subtotal.add(dto.subtotal);

            totalQuantity += item.getQuantity();
            totalAmount = totalAmount.add(dto.subtotal);
        }

        if (!invalidItems.isEmpty()) {
            cartItemRepository.deleteAll(invalidItems);
        }

        detail.shops = new ArrayList<>(shops.values());
        detail.totalItems = totalQuantity;
        detail.totalAmount = totalAmount;
        return detail;
    }
}
