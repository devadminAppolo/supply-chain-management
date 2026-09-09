package com.supplychain.wms.receiving.infrastructure.repository;

import com.supplychain.wms.receiving.domain.model.ReceivingPallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivingPalletRepository extends JpaRepository<ReceivingPallet, Long> {

    Optional<ReceivingPallet> findByIdAndDeletedFalse(Long id);

    List<ReceivingPallet> findByReceivingIdAndDeletedFalse(Long receivingId);

    List<ReceivingPallet> findByAdvanceShippingNoticeIdAndDeletedFalse(Long asnId);

    List<ReceivingPallet> findByAdvanceShippingNoticeLineIdAndDeletedFalse(Long asnLineId);

    @Query("SELECT COALESCE(SUM(rp.quantity), 0) FROM ReceivingPallet rp " +
           "WHERE rp.advanceShippingNoticeLine.id = :asnLineId " +
           "AND rp.receiving.receivingStatus != com.supplychain.wms.receiving.domain.enums.ReceivingStatus.CANCELLED " +
           "AND rp.deleted = false")
    BigDecimal sumReceivedQuantityByAsnLineId(@Param("asnLineId") Long asnLineId);

    @Query("SELECT COALESCE(SUM(rp.quantity), 0) FROM ReceivingPallet rp " +
           "WHERE rp.purchaseOrderLine.id = :poLineId " +
           "AND rp.receiving.receivingStatus = com.supplychain.wms.receiving.domain.enums.ReceivingStatus.COMPLETED " +
           "AND rp.deleted = false")
    BigDecimal sumReceivedQuantityByPoLineId(@Param("poLineId") Long poLineId);
}
