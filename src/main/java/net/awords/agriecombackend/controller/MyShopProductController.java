package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.product.ProductDtos;
import net.awords.agriecombackend.service.ShopProductService;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * 商户后台商品管理接口，仅可操作自己店铺下的商品。
 */
@RestController
@RequestMapping("/api/v1/my-shop/products")
@Tag(name = "My Shop Products", description = "商户管理店铺商品")
@PreAuthorize("hasRole(T(net.awords.agriecombackend.security.RoleConstants).MERCHANT)")
public class MyShopProductController {

    private final ShopProductService shopProductService;

    public MyShopProductController(ShopProductService shopProductService) {
        this.shopProductService = shopProductService;
    }

    @Operation(summary = "获取店铺商品列表")
    @GetMapping
    public ApiResponseDTO<List<ProductDtos.Detail>> list(Authentication authentication) {
        return ApiResponseDTO.success(shopProductService.list(requireUsername(authentication)));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponseDTO<ProductDtos.Detail> create(@Valid @RequestBody ProductDtos.CreateRequest request,
                                                     Authentication authentication) {
        return ApiResponseDTO.success(shopProductService.create(requireUsername(authentication), request));
    }

    @Operation(summary = "查看商品详情")
    @GetMapping("/{productId}")
    public ApiResponseDTO<ProductDtos.Detail> get(@PathVariable Long productId, Authentication authentication) {
        return ApiResponseDTO.success(shopProductService.get(requireUsername(authentication), productId));
    }

    @Operation(summary = "更新商品信息")
    @PutMapping("/{productId}")
    public ApiResponseDTO<ProductDtos.Detail> update(@PathVariable Long productId,
                                                     @Valid @RequestBody ProductDtos.UpdateRequest request,
                                                     Authentication authentication) {
        return ApiResponseDTO.success(shopProductService.update(requireUsername(authentication), productId, request));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{productId}")
    public ApiResponseDTO<Void> delete(@PathVariable Long productId, Authentication authentication) {
        shopProductService.delete(requireUsername(authentication), productId);
        return ApiResponseDTO.success(null);
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
