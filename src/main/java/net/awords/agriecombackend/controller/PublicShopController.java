package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.service.ShopPublicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@Tag(name = "Public Shop", description = "买家店铺浏览接口")
public class PublicShopController {

    private final ShopPublicService shopPublicService;

    public PublicShopController(ShopPublicService shopPublicService) {
        this.shopPublicService = shopPublicService;
    }

    @Operation(summary = "浏览店铺列表", description = "支持分页与名称关键字搜索", parameters = {
            @Parameter(name = "page", description = "页码（从0开始）", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "每页数据量，最大50", in = ParameterIn.QUERY),
            @Parameter(name = "keyword", description = "店铺名称关键字", in = ParameterIn.QUERY)
    })
    @GetMapping
    public ApiResponseDTO<ShopDtos.PublicPage> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                                    @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponseDTO.success(shopPublicService.list(page, size, keyword));
    }

    @Operation(summary = "查看店铺主页", description = "返回店铺公开信息及商品列表")
    @GetMapping("/{shopId}")
    public ApiResponseDTO<ShopDtos.PublicDetail> get(@PathVariable Long shopId) {
        return ApiResponseDTO.success(shopPublicService.getPublicDetail(shopId));
    }
}
