package com.supplychain.procurement.asn.infrastructure.repository;

import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvanceShippingNoticeRepository extends JpaRepository<AdvanceShippingNotice, Long>, JpaSpecificationExecutor<AdvanceShippingNotice> {

    boolean existsByAsnNumberAndDeletedFalse(String asnNumber);

    boolean existsByAsnNumberAndIdNotAndDeletedFalse(String asnNumber, Long id);

    Optional<AdvanceShippingNotice> findByIdAndDeletedFalse(Long id);

    Optional<AdvanceShippingNotice> findByAsnNumberAndDeletedFalse(String asnNumber);

    List<AdvanceShippingNotice> findByPurchaseOrderIdAndDeletedFalse(Long purchaseOrderId);

    List<AdvanceShippingNotice> findAllByDeletedFalseOrderByAsnNumberAsc();
}
