package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.cart.CartDtos;
import net.awords.agriecombackend.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 购物车接口，支持增删改查。
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "购物车接口")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "查看购物车")
    @GetMapping
    public ApiResponseDTO<CartDtos.CartDetail> get(Authentication authentication) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(cartService.getCart(username));
    }

    @Operation(summary = "加入购物车")
    @PostMapping("/items")
    public ApiResponseDTO<CartDtos.CartDetail> add(Authentication authentication,
                                                   @Valid @RequestBody CartDtos.AddItemRequest request) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(cartService.addItem(username, request));
    }

    @Operation(summary = "更新购物车数量")
    @PutMapping("/items/{productId}")
    public ApiResponseDTO<CartDtos.CartDetail> update(Authentication authentication,
                                                      @PathVariable Long productId,
                                                      @Valid @RequestBody CartDtos.UpdateItemRequest request) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(cartService.updateItem(username, productId, request));
    }

    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/items/{productId}")
    public ApiResponseDTO<CartDtos.CartDetail> delete(Authentication authentication,
                                                      @PathVariable Long productId) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(cartService.removeItem(username, productId));
    }

    @Operation(summary = "清空购物车")
    @PostMapping("/clear")
    public ApiResponseDTO<CartDtos.CartDetail> clear(Authentication authentication) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(cartService.clear(username));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
