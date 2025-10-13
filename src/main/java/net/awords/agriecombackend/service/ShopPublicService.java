package net.awords.agriecombackend.service;

import net.awords.agriecombackend.dto.shop.ShopDtos;
import net.awords.agriecombackend.dto.shop.ShopMapper;
import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import net.awords.agriecombackend.repository.ProductRepository;
import net.awords.agriecombackend.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * 面向买家的店铺浏览服务，支持分页与关键字搜索。
 */
@Service
public class ShopPublicService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public ShopPublicService(ShopRepository shopRepository, ProductRepository productRepository) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ShopDtos.PublicPage list(int page, int size, String keyword) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : Math.min(size, 50);
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Shop> resultPage;
        if (StringUtils.hasText(keyword)) {
            resultPage = shopRepository.findByStatusAndNameContainingIgnoreCase(ShopStatus.ACTIVE, keyword.trim(), pageable);
        } else {
            resultPage = shopRepository.findByStatus(ShopStatus.ACTIVE, pageable);
        }

        ShopDtos.PublicPage pageDto = new ShopDtos.PublicPage();
        pageDto.items = resultPage.getContent().stream()
                .map(shop -> ShopMapper.toPublicSummary(shop, productRepository.countByShopId(shop.getId())))
                .toList();
        pageDto.totalElements = resultPage.getTotalElements();
        pageDto.totalPages = resultPage.getTotalPages();
        pageDto.page = resultPage.getNumber();
        pageDto.size = resultPage.getSize();
        return pageDto;
    }

    @Transactional(readOnly = true)
    public ShopDtos.PublicDetail getPublicDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .filter(s -> s.getStatus() == ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "店铺不存在或未上线"));

        long productCount = productRepository.countByShopId(shop.getId());
        var products = productRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());
        return ShopMapper.toPublicDetail(shop, productCount, products);
    }
}
