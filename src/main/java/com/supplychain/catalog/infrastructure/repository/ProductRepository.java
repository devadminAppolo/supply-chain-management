package com.supplychain.catalog.infrastructure.repository;

import com.supplychain.catalog.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByProductCodeAndDeletedFalse(String productCode);

    boolean existsByProductCodeAndIdNotAndDeletedFalse(String productCode, Long id);

    boolean existsByBarcodeAndDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, Long id);

    List<Product> findAllByDeletedFalseOrderByProductNameAsc();

    Optional<Product> findByProductNameAndDeletedFalse(String productName);

    Optional<Product> findByProductCodeAndDeletedFalse(String productCode);
}
