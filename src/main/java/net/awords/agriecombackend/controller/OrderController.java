package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.order.OrderDtos;
import net.awords.agriecombackend.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * 买家订单接口：下单、查询及取消。
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Orders", description = "买家订单操作")
@PreAuthorize("hasRole(T(net.awords.agriecombackend.security.RoleConstants).USER)")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "创建订单", description = "支持自动将不同店铺商品拆单")
    @PostMapping("/orders")
    public ApiResponseDTO<OrderDtos.OrderDetail> create(@Valid @RequestBody OrderDtos.CreateOrderRequest request,
                                                        Authentication authentication) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(orderService.create(username, request));
    }

    @Operation(summary = "我的订单列表", description = "默认分页大小 10，可调整 page/size 参数")
    @GetMapping("/my-orders")
    public ApiResponseDTO<OrderDtos.PageResult<OrderDtos.OrderSummary>> list(Authentication authentication,
                                                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                                                             @RequestParam(name = "size", defaultValue = "10") int size) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(orderService.list(username, page, size));
    }

    @Operation(summary = "订单详情", description = "包含子订单与订单项列表")
    @GetMapping("/my-orders/{orderId}")
    public ApiResponseDTO<OrderDtos.OrderDetail> detail(Authentication authentication,
                                                        @PathVariable Long orderId) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(orderService.get(username, orderId));
    }

    @Operation(summary = "取消订单", description = "未发货订单可取消")
    @PutMapping("/my-orders/{orderId}/cancel")
    public ApiResponseDTO<OrderDtos.OrderDetail> cancel(Authentication authentication,
                                                        @PathVariable Long orderId) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(orderService.cancel(username, orderId));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
