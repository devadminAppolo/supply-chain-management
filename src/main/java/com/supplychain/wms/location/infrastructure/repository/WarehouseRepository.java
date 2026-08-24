package com.supplychain.wms.location.infrastructure.repository;

import com.supplychain.wms.location.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {

    boolean existsByWarehouseCodeAndDeletedFalse(String warehouseCode);

    boolean existsByWarehouseCodeAndIdNotAndDeletedFalse(String warehouseCode, Long id);

    List<Warehouse> findAllByDeletedFalseOrderByWarehouseNameAsc();

    Optional<Warehouse> findByWarehouseNameAndDeletedFalse(String warehouseName);
}
