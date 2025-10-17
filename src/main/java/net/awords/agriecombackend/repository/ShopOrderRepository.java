package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    @EntityGraph(attributePaths = {"orderGroup", "orderGroup.buyer", "shop", "orderItems", "orderItems.product"})
    Page<ShopOrder> findByShopOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"orderGroup", "orderGroup.buyer", "shop", "orderItems", "orderItems.product"})
    Optional<ShopOrder> findByIdAndShopOwnerId(Long id, Long ownerId);

    @EntityGraph(attributePaths = {"orderGroup", "orderGroup.buyer", "shop", "orderItems", "orderItems.product"})
    java.util.List<ShopOrder> findByOrderGroupId(Long orderGroupId);
}
