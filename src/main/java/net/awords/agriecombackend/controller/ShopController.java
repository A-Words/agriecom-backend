package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Shop", description = "商户店铺管理")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @Operation(summary = "申请开店", description = "提交店铺基础信息，状态将置为待审核")
    @PostMapping("/shops")
    public ApiResponseDTO<ShopDtos.DetailResponse> apply(@Valid @RequestBody ShopDtos.CreateRequest request,
                                                         Authentication authentication) {
        ShopDtos.DetailResponse detail = shopService.applyForShop(requireUsername(authentication), request);
        return ApiResponseDTO.success(detail);
    }

    @Operation(summary = "查看我的店铺", description = "返回当前商户的店铺详情与统计信息")
    @GetMapping("/my-shop")
    public ApiResponseDTO<ShopDtos.DetailResponse> myShop(Authentication authentication) {
        ShopDtos.DetailResponse detail = shopService.getMyShop(requireUsername(authentication));
        return ApiResponseDTO.success(detail);
    }

    @Operation(summary = "更新我的店铺信息")
    @PutMapping("/my-shop")
    public ApiResponseDTO<ShopDtos.DetailResponse> updateMyShop(@Valid @RequestBody ShopDtos.UpdateRequest request,
                                                                Authentication authentication) {
        ShopDtos.DetailResponse detail = shopService.updateMyShop(requireUsername(authentication), request);
        return ApiResponseDTO.success(detail);
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
