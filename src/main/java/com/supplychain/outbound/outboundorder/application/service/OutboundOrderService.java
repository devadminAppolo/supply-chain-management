package com.supplychain.outbound.outboundorder.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderCreateRequestDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderResponseDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderUpdateRequestDto;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OutboundOrderService {

    /** Create a new outbound order in DRAFT status. */
    OutboundOrderResponseDto createOutboundOrder(OutboundOrderCreateRequestDto request);

    /**
     * Update an existing outbound order.
     * Only allowed when status is DRAFT or PENDING.
     */
    OutboundOrderResponseDto updateOutboundOrder(Long id, OutboundOrderUpdateRequestDto request);

    /** Retrieve a single outbound order by its database ID. */
    OutboundOrderResponseDto findById(Long id);

    /** Paginated list with optional filters. */
    PageResponse<OutboundOrderResponseDto> findAll(String search,
                                                   OutboundOrderStatus orderStatus,
                                                   OutboundOrderType orderType,
                                                   String customerCode,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable);

    /** Minimal list for UI dropdown widgets. */
    List<DropdownOptionDto> findDropdown();

    /**
     * Confirm an order – transitions DRAFT or PENDING → CONFIRMED.
     * No inventory impact; confirmation is a logical approval step only.
     */
    OutboundOrderResponseDto confirmOutboundOrder(Long id);

    /**
     * Cancel an order – transitions DRAFT, PENDING, or CONFIRMED → CANCELLED.
     * No inventory impact.
     */
    OutboundOrderResponseDto cancelOutboundOrder(Long id);

    /**
     * Soft-delete an outbound order.
     * Only DRAFT orders may be physically soft-deleted; confirmed/processed
     * orders must be cancelled first.
     */
    void deleteById(Long id);

    /** Bulk soft-delete. Each record must be in DRAFT status. */
    void deleteAllByIds(List<Long> ids);
}
