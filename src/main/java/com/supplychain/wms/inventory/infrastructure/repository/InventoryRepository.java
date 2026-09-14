package com.supplychain.wms.inventory.infrastructure.repository;

import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.wms.inventory.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    @Query("""
            SELECT COUNT(i) > 0 FROM Inventory i
            WHERE i.warehouseId     = :warehouseId
              AND i.binId           = :binId
              AND i.skuId           = :skuId
              AND COALESCE(i.lotNumber,    '') = COALESCE(:lotNumber,    '')
              AND COALESCE(i.serialNumber, '') = COALESCE(:serialNumber, '')
              AND i.inventoryStatus = :inventoryStatus
              AND i.deleted         = false
            """)
    boolean existsDuplicateOnCreate(
            @Param("warehouseId")     Long warehouseId,
            @Param("binId")           Long binId,
            @Param("skuId")           Long skuId,
            @Param("lotNumber")       String lotNumber,
            @Param("serialNumber")    String serialNumber,
            @Param("inventoryStatus") InventoryStatus inventoryStatus
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM Inventory i
            WHERE i.warehouseId     = :warehouseId
              AND i.binId           = :binId
              AND i.skuId           = :skuId
              AND COALESCE(i.lotNumber,    '') = COALESCE(:lotNumber,    '')
              AND COALESCE(i.serialNumber, '') = COALESCE(:serialNumber, '')
              AND i.inventoryStatus = :inventoryStatus
              AND i.deleted         = false
            """)
    Optional<Inventory> findInventoryByKey(
            @Param("warehouseId")     Long warehouseId,
            @Param("binId")           Long binId,
            @Param("skuId")           Long skuId,
            @Param("lotNumber")       String lotNumber,
            @Param("serialNumber")    String serialNumber,
            @Param("inventoryStatus") InventoryStatus inventoryStatus
    );

    @Query("""
            SELECT SUM(i.onHandQuantity),
                   SUM(i.availableQuantity),
                   SUM(i.reservedQuantity),
                   SUM(i.allocatedQuantity)
            FROM Inventory i
            WHERE i.deleted = false
            """)
    List<Object[]> sumAllQuantities();

    @Query("""
            SELECT i.warehouseId,
                   SUM(i.onHandQuantity),
                   SUM(i.availableQuantity),
                   SUM(i.reservedQuantity),
                   SUM(i.allocatedQuantity)
            FROM Inventory i
            WHERE i.deleted = false
            GROUP BY i.warehouseId
            """)
    List<Object[]> sumQuantitiesByWarehouse();

    @Query("""
            SELECT i.inventoryStatus,
                   SUM(i.onHandQuantity)
            FROM Inventory i
            WHERE i.deleted = false
            GROUP BY i.inventoryStatus
            """)
    List<Object[]> sumQuantitiesByStatus();

    /**
     * Finds all AVAILABLE inventory records for a given SKU that have remaining
     * available quantity, ordered by ID (FIFO) for allocation.
     * Uses PESSIMISTIC_WRITE to prevent concurrent double-allocation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM Inventory i
            WHERE i.skuId           = :skuId
              AND i.inventoryStatus = com.supplychain.common.domain.enums.InventoryStatus.AVAILABLE
              AND i.availableQuantity > 0
              AND i.deleted         = false
            ORDER BY i.id ASC
            """)
    List<Inventory> findAvailableInventoryBySkuForUpdate(@Param("skuId") Long skuId);

    @Query("""
            SELECT COUNT(i) > 0 FROM Inventory i
            WHERE i.warehouseId     = :warehouseId
              AND i.binId           = :binId
              AND i.skuId           = :skuId
              AND COALESCE(i.lotNumber,    '') = COALESCE(:lotNumber,    '')
              AND COALESCE(i.serialNumber, '') = COALESCE(:serialNumber, '')
              AND i.inventoryStatus = :inventoryStatus
              AND i.deleted         = false
              AND i.id             <> :excludeId
            """)
    boolean existsDuplicateOnUpdate(
            @Param("warehouseId")     Long warehouseId,
            @Param("binId")           Long binId,
            @Param("skuId")           Long skuId,
            @Param("lotNumber")       String lotNumber,
            @Param("serialNumber")    String serialNumber,
            @Param("inventoryStatus") InventoryStatus inventoryStatus,
            @Param("excludeId")       Long excludeId
    );
}
