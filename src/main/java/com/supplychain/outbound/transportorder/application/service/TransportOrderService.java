package com.supplychain.outbound.transportorder.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderCreateRequestDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderResponseDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderUpdateRequestDto;
import com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransportOrderService {

    /**
     * Creates a new Transport Order from one or more READY_TO_SHIP Outbound Orders.
     * All validations (existence, status, origin compatibility, duplicate assignment)
     * are performed within a single transaction.
     */
    TransportOrderResponseDto createTransportOrder(TransportOrderCreateRequestDto request);

    /**
     * Returns a Transport Order by its primary key.
     * Includes associated Outbound Orders and line items.
     */
    TransportOrderResponseDto findById(Long id);

    /**
     * Returns a paginated list of Transport Orders with optional filters.
     *
     * @param search     free-text match against TO number
     * @param toStatus   exact status filter
     * @param mode       exact mode filter
     * @param startDate  pickup window start date (inclusive)
     * @param endDate    pickup window end date (inclusive)
     * @param pageable   Spring pagination and sort descriptor
     */
    PageResponse<TransportOrderResponseDto> findAll(String search,
                                                    TransportOrderStatus toStatus,
                                                    String mode,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    Pageable pageable);

    /**
     * Updates mutable header fields on an existing Transport Order.
     * Only DRAFT and PENDING TOs may be updated.
     * Associated Outbound Orders and line items are not modifiable in Phase 1.
     */
    TransportOrderResponseDto updateTransportOrder(Long id, TransportOrderUpdateRequestDto request);

    /**
     * Cancels a Transport Order.
     * Only DRAFT and PENDING TOs may be cancelled.
     * Associated Outbound Orders become available for reassignment after cancellation.
     */
    TransportOrderResponseDto cancelTransportOrder(Long id);
}
