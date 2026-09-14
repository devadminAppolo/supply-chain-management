package com.supplychain.outbound.packing.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import com.supplychain.outbound.packing.application.dto.BulkAssignPackingRequestDto;
import com.supplychain.outbound.packing.application.dto.CompletePackingRequestDto;
import com.supplychain.outbound.packing.application.dto.PackingTaskResponseDto;
import com.supplychain.outbound.packing.domain.enums.PackingStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PackingService {

    /**
     * Creates packing tasks for an outbound order once all picking is complete.
     * Called automatically by {@code PickingService} after the last pick task
     * is completed. A single consolidated packing task is created for the order.
     *
     * @param order       the fully-loaded outbound order (status should be PICKED)
     * @param totalQty    total quantity to pack (sum of all quantities picked)
     * @param quantityUom unit of measure for the quantity
     */
    void createPackingTasksForOrder(OutboundOrder order, BigDecimal totalQty, String quantityUom);

    /** Paginated list with optional filters. */
    PageResponse<PackingTaskResponseDto> findAll(String search,
                                                  PackingStatus status,
                                                  Long outboundOrderId,
                                                  String staffId,
                                                  Pageable pageable);

    /** Full detail for a single packing task. */
    PackingTaskResponseDto findById(Long id);

    /**
     * Bulk-assigns packing tasks to a staff member.
     * All tasks must be PENDING; validation is all-or-nothing.
     */
    List<PackingTaskResponseDto> bulkAssign(BulkAssignPackingRequestDto request);

    /**
     * Transitions a packing task: ASSIGNED → IN_PROGRESS.
     * Sets startedAt timestamp.
     */
    PackingTaskResponseDto startPacking(Long id);

    /**
     * Completes a packing task: IN_PROGRESS → COMPLETED.
     * <ul>
     *   <li>Validates quantity packed.</li>
     *   <li>Assigns a generated packageId to the task.</li>
     *   <li>Records physical dimensions (weight, volume).</li>
     *   <li>Checks whether all packing tasks for the parent order are done.</li>
     *   <li>If all done: transitions order to READY_TO_SHIP.</li>
     * </ul>
     */
    PackingTaskResponseDto completePacking(Long id, CompletePackingRequestDto request);
}
