package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.service.ShopAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shops")
@Tag(name = "Admin Shop", description = "平台管理员店铺审核接口")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShopController {

    private final ShopAdminService shopAdminService;

    public AdminShopController(ShopAdminService shopAdminService) {
        this.shopAdminService = shopAdminService;
    }

    @Operation(summary = "按状态查询店铺申请", parameters = {
            @Parameter(name = "status", description = "店铺状态，默认 PENDING_REVIEW", in = ParameterIn.QUERY)
    })
    @GetMapping
    public ApiResponseDTO<List<ShopDtos.DetailResponse>> listByStatus(@RequestParam(name = "status", defaultValue = "PENDING_REVIEW") ShopStatus status) {
        return ApiResponseDTO.success(shopAdminService.listByStatus(status));
    }

    @Operation(summary = "审核通过店铺")
    @PutMapping("/{shopId}/approve")
    public ApiResponseDTO<ShopDtos.DetailResponse> approve(@PathVariable Long shopId) {
        return ApiResponseDTO.success(shopAdminService.approve(shopId));
    }

    @Operation(summary = "驳回店铺申请")
    @PutMapping("/{shopId}/reject")
    public ApiResponseDTO<ShopDtos.DetailResponse> reject(@PathVariable Long shopId) {
        return ApiResponseDTO.success(shopAdminService.reject(shopId));
    }

    @Operation(summary = "封禁店铺")
    @PutMapping("/{shopId}/suspend")
    public ApiResponseDTO<ShopDtos.DetailResponse> suspend(@PathVariable Long shopId) {
        return ApiResponseDTO.success(shopAdminService.suspend(shopId));
    }
}
