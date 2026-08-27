package com.supplychain.procurement.asn.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.asn.application.dto.AsnCreateRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnUpdateRequestDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderLineDto;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AdvanceShippingNoticeService {

    AsnResponseDto createAsn(AsnCreateRequestDto request);

    AsnResponseDto updateAsn(Long id, AsnUpdateRequestDto request);

    AsnResponseDto findById(Long id);

    PageResponse<AsnResponseDto> findAll(String search,
                                         AsnStatus status,
                                         Long purchaseOrderId,
                                         String supplierId,
                                         String warehouseId,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         Pageable pageable);

    AsnResponseDto cancelAsn(Long id);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);

    PageResponse<AvailablePurchaseOrderDto> getAvailablePurchaseOrders(String search, Pageable pageable);

    List<AvailablePurchaseOrderLineDto> getAvailablePoLines(Long poId);
}
