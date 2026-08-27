package com.supplychain.procurement.asn.infrastructure.repository;

import com.supplychain.procurement.asn.domain.model.AdvanceShippingNoticeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AdvanceShippingNoticeLineRepository extends JpaRepository<AdvanceShippingNoticeLine, Long>, JpaSpecificationExecutor<AdvanceShippingNoticeLine> {

    List<AdvanceShippingNoticeLine> findByAdvanceShippingNoticeIdAndDeletedFalseOrderByLineNumberAsc(Long asnId);

    List<AdvanceShippingNoticeLine> findByPurchaseOrderLineIdAndDeletedFalse(Long poLineId);

    @Query("""
            SELECT COALESCE(SUM(l.shipmentQuantity), 0)
            FROM AdvanceShippingNoticeLine l
            WHERE l.purchaseOrderLine.id = :poLineId
              AND l.deleted = false
              AND l.advanceShippingNotice.deleted = false
              AND l.advanceShippingNotice.asnStatus <> com.supplychain.procurement.asn.domain.enums.AsnStatus.CANCELLED
            """)
    BigDecimal sumShippedQuantityByPoLineId(@Param("poLineId") Long poLineId);

    @Query("""
            SELECT COALESCE(SUM(l.shipmentQuantity), 0)
            FROM AdvanceShippingNoticeLine l
            WHERE l.purchaseOrderLine.id = :poLineId
              AND l.advanceShippingNotice.id <> :excludeAsnId
              AND l.deleted = false
              AND l.advanceShippingNotice.deleted = false
              AND l.advanceShippingNotice.asnStatus <> com.supplychain.procurement.asn.domain.enums.AsnStatus.CANCELLED
            """)
    BigDecimal sumShippedQuantityByPoLineIdExcludingAsn(@Param("poLineId") Long poLineId, @Param("excludeAsnId") Long excludeAsnId);
}
