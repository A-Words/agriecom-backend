package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.entity.Product;
import net.awords.agriecombackend.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "商品相关接口")
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Operation(summary = "列出所有商品")
    @GetMapping
    public ApiResponseDTO<List<Product>> list() {
        return ApiResponseDTO.success(productRepository.findAll());
    }
}
