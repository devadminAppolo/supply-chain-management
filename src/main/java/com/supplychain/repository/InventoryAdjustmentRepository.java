package com.supplychain.repository;

import com.supplychain.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventoryAdjustmentRepository
        extends JpaRepository<InventoryAdjustment, Long>, JpaSpecificationExecutor<InventoryAdjustment> {

    // -------------------------------------------------------------------------
    // Dashboard queries
    // -------------------------------------------------------------------------

    /** Counts active adjustments whose adjustmentDate falls within [startOfDay, endOfDay). */
    @Query("""
            SELECT COUNT(a)
            FROM InventoryAdjustment a
            WHERE a.deleted = false
              AND a.adjustmentDate >= :startOfDay
              AND a.adjustmentDate <  :endOfDay
            """)
    long countTodayAdjustments(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay")   LocalDateTime endOfDay
    );
}
