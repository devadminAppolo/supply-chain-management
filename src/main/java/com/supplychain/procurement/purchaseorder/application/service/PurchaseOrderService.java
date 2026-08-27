package com.supplychain.procurement.purchaseorder.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import org.springframework.data.domain.Pageable;

import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderLineDto;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderDto save(PurchaseOrderDto dto);

    PurchaseOrderDto update(Long id, PurchaseOrderDto dto);

    PurchaseOrderDto findById(Long id);

    PageResponse<PurchaseOrderDto> findAll(String search,
                                           PoStatus poStatus,
                                           String supplier,
                                           String warehouse,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);

    PageResponse<AvailablePurchaseOrderDto> getAvailableForAsn(String search, Pageable pageable);

    List<AvailablePurchaseOrderLineDto> getAvailableAsnLines(Long poId);
}
