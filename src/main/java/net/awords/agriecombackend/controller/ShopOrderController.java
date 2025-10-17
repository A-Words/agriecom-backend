package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.order.OrderDtos;
import net.awords.agriecombackend.service.ShopOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 商户端订单管理入口。
 */
@RestController
@RequestMapping("/api/v1/my-shop/orders")
@Tag(name = "Shop Orders", description = "商户订单管理")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;

    public ShopOrderController(ShopOrderService shopOrderService) {
        this.shopOrderService = shopOrderService;
    }

    @Operation(summary = "查看店铺订单列表")
    @GetMapping
    public ApiResponseDTO<OrderDtos.PageResult<OrderDtos.ShopOrderSummary>> list(Authentication authentication,
                                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(shopOrderService.list(username, page, size));
    }

    @Operation(summary = "查看店铺订单详情")
    @GetMapping("/{orderId}")
    public ApiResponseDTO<OrderDtos.ShopOrderDetail> detail(Authentication authentication,
                                                            @PathVariable Long orderId) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(shopOrderService.get(username, orderId));
    }

    @Operation(summary = "订单发货", description = "更新物流信息并标记为已发货")
    @PutMapping("/{orderId}/ship")
    public ApiResponseDTO<OrderDtos.ShopOrderDetail> ship(Authentication authentication,
                                                          @PathVariable Long orderId,
                                                          @Valid @RequestBody(required = false) OrderDtos.ShipRequest request) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(shopOrderService.ship(username, orderId, request));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
