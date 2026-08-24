package com.supplychain.wms.inventorytransaction.infrastructure.repository;

import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {

    @Query("""
            SELECT COUNT(t)
            FROM InventoryTransaction t
            WHERE t.deleted = false
              AND t.transactionDate >= :startOfDay
              AND t.transactionDate <  :endOfDay
            """)
    long countTodayTransactions(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay")   LocalDateTime endOfDay
    );

    @Query("""
            SELECT t
            FROM InventoryTransaction t
            WHERE t.deleted = false
            ORDER BY t.transactionDate DESC
            """)
    List<InventoryTransaction> findRecentTransactions(Pageable pageable);
}
