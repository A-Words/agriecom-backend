package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.Shop;
import net.awords.agriecombackend.entity.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Shop 仓储接口，为后续多租户隔离提供数据访问层。
 */
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long ownerId);

    List<Shop> findAllByStatusOrderByCreatedAtAsc(ShopStatus status);

    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    Page<Shop> findByStatusAndNameContainingIgnoreCase(ShopStatus status, String keyword, Pageable pageable);
}
