package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
	long countByShopId(Long shopId);

	java.util.List<Product> findAllByShopIdOrderByCreatedAtDesc(Long shopId);

	java.util.List<Product> findByShopIdOrderByCreatedAtDesc(Long shopId);

	java.util.Optional<Product> findByIdAndShopId(Long id, Long shopId);
}
