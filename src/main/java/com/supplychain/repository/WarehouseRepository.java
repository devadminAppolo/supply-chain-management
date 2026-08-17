package com.supplychain.repository;

import com.supplychain.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {

    boolean existsByWarehouseCodeAndDeletedFalse(String warehouseCode);

    boolean existsByWarehouseCodeAndIdNotAndDeletedFalse(String warehouseCode, Long id);

    List<Warehouse> findAllByDeletedFalseOrderByWarehouseNameAsc();

    java.util.Optional<Warehouse> findByWarehouseNameAndDeletedFalse(String warehouseName);
}
