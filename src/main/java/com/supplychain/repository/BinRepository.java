package com.supplychain.repository;

import com.supplychain.entity.Bin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BinRepository extends JpaRepository<Bin, Long>, JpaSpecificationExecutor<Bin> {

    boolean existsByBinCodeAndRackIdAndDeletedFalse(String binCode, Long rackId);

    boolean existsByBinCodeAndRackIdAndIdNotAndDeletedFalse(String binCode, Long rackId, Long id);

    boolean existsByBarcodeAndDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, Long id);

    List<Bin> findAllByRackIdAndDeletedFalseOrderByBinCodeAsc(Long rackId);

    java.util.Optional<Bin> findByBinCodeAndRackIdAndDeletedFalse(String binCode, Long rackId);

    java.util.Optional<Bin> findByBinCodeAndWarehouseIdAndDeletedFalse(String binCode, Long warehouseId);
}
