package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.OrderGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    @EntityGraph(attributePaths = {"shopOrders", "shopOrders.shop", "shopOrders.orderItems", "shopOrders.orderItems.product"})
    Page<OrderGroup> findByBuyerId(Long buyerId, Pageable pageable);

    @EntityGraph(attributePaths = {"shopOrders", "shopOrders.shop", "shopOrders.orderItems", "shopOrders.orderItems.product"})
    Optional<OrderGroup> findByIdAndBuyerId(Long id, Long buyerId);
}
