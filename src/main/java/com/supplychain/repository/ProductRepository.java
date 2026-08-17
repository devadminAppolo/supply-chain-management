package com.supplychain.repository;

import com.supplychain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByProductCodeAndDeletedFalse(String productCode);

    boolean existsByProductCodeAndIdNotAndDeletedFalse(String productCode, Long id);

    boolean existsByBarcodeAndDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, Long id);

    List<Product> findAllByDeletedFalseOrderByProductNameAsc();

    java.util.Optional<Product> findByProductNameAndDeletedFalse(String productName);
}
