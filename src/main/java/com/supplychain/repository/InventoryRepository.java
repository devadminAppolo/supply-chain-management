package com.supplychain.repository;

import com.supplychain.entity.Inventory;
import com.supplychain.enums.InventoryStatus;
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

    /**
     * Duplicate detection for create: checks whether an active inventory record already
     * exists for the same warehouse + bin + SKU + lot + serial + status combination.
     * COALESCE normalises NULLs so two NULL lot/serial values are treated as equal.
     */
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

    /**
     * Finds the active inventory record for a given location + SKU + tracking combination.
     * A pessimistic write lock (SELECT … FOR UPDATE) is acquired so that concurrent
     * adjustment transactions queue up rather than producing lost updates.
     */
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

    // -------------------------------------------------------------------------
    // Dashboard aggregate queries
    // -------------------------------------------------------------------------

    /** Sums all quantity columns across active inventory records. Returns one Object[] row. */
    @Query("""
            SELECT SUM(i.onHandQuantity),
                   SUM(i.availableQuantity),
                   SUM(i.reservedQuantity),
                   SUM(i.allocatedQuantity)
            FROM Inventory i
            WHERE i.deleted = false
            """)
    List<Object[]> sumAllQuantities();

    /**
     * Groups active inventory by warehouse and sums quantity columns.
     * Each row: [warehouseId, onHand, available, reserved, allocated].
     */
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

    /**
     * Groups active inventory by status and sums on-hand quantity.
     * Each row: [inventoryStatus, totalOnHand].
     */
    @Query("""
            SELECT i.inventoryStatus,
                   SUM(i.onHandQuantity)
            FROM Inventory i
            WHERE i.deleted = false
            GROUP BY i.inventoryStatus
            """)
    List<Object[]> sumQuantitiesByStatus();

    /**
     * Duplicate detection for update: same as above but excludes the record being updated.
     */
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
