package net.awords.agriecombackend.repository;

import net.awords.agriecombackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
	long countByShopId(Long shopId);

	java.util.List<Product> findAllByShopIdOrderByPublishedAtDesc(Long shopId);

	java.util.List<Product> findByShopIdOrderByPublishedAtDesc(Long shopId);

	java.util.Optional<Product> findByIdAndShopId(Long id, Long shopId);
}
