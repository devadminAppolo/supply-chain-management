package com.supplychain.repository;

import com.supplychain.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long>, JpaSpecificationExecutor<Sku> {

    boolean existsBySkuCodeAndProductIdAndDeletedFalse(String skuCode, Long productId);

    boolean existsBySkuCodeAndProductIdAndIdNotAndDeletedFalse(String skuCode, Long productId, Long id);

    boolean existsByUpcAndDeletedFalse(String upc);

    boolean existsByUpcAndIdNotAndDeletedFalse(String upc, Long id);

    List<Sku> findAllByDeletedFalseOrderBySkuCodeAsc();

    List<Sku> findAllByProductIdAndDeletedFalseOrderBySkuCodeAsc(Long productId);

    java.util.Optional<Sku> findBySkuCodeAndDeletedFalse(String skuCode);

    java.util.Optional<Sku> findBySkuCodeAndProductIdAndDeletedFalse(String skuCode, Long productId);
}
