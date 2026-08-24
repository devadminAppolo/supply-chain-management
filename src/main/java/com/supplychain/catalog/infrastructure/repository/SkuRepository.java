package com.supplychain.catalog.infrastructure.repository;

import com.supplychain.catalog.domain.model.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long>, JpaSpecificationExecutor<Sku> {

    boolean existsBySkuCodeAndProductIdAndDeletedFalse(String skuCode, Long productId);

    boolean existsBySkuCodeAndProductIdAndIdNotAndDeletedFalse(String skuCode, Long productId, Long id);

    boolean existsByUpcAndDeletedFalse(String upc);

    boolean existsByUpcAndIdNotAndDeletedFalse(String upc, Long id);

    List<Sku> findAllByDeletedFalseOrderBySkuCodeAsc();

    List<Sku> findAllByProductIdAndDeletedFalseOrderBySkuCodeAsc(Long productId);

    Optional<Sku> findBySkuCodeAndDeletedFalse(String skuCode);

    Optional<Sku> findBySkuCodeAndProductIdAndDeletedFalse(String skuCode, Long productId);
}
