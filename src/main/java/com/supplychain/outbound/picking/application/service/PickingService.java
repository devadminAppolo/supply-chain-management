package com.supplychain.outbound.picking.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.picking.application.dto.BulkAssignPickingRequestDto;
import com.supplychain.outbound.picking.application.dto.CompletePickingRequestDto;
import com.supplychain.outbound.picking.application.dto.PickingTaskResponseDto;
import com.supplychain.outbound.picking.domain.enums.PickingStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PickingService {

    /**
     * Allocates available inventory for all lines of the given outbound order
     * and creates PENDING pick tasks for each allocated bin/quantity.
     * Called automatically when an outbound order is confirmed.
     *
     * @throws org.springframework.web.server.ResponseStatusException if no
     *         inventory is available for any order line.
     */
    void allocateAndCreatePickTasks(Long orderId);

    /** Paginated list with optional filters. */
    PageResponse<PickingTaskResponseDto> findAll(String search,
                                                  PickingStatus status,
                                                  Long outboundOrderId,
                                                  String staffId,
                                                  Pageable pageable);

    /** Full detail for a single pick task. */
    PickingTaskResponseDto findById(Long id);

    /**
     * Bulk-assigns pick tasks to a staff member.
     * All tasks must be PENDING; validation is all-or-nothing.
     */
    List<PickingTaskResponseDto> bulkAssign(BulkAssignPickingRequestDto request);

    /**
     * Transitions a pick task: ASSIGNED → IN_PROGRESS.
     * Sets startedAt timestamp.
     */
    PickingTaskResponseDto startPicking(Long id);

    /**
     * Completes a pick task: IN_PROGRESS → COMPLETED.
     * <ul>
     *   <li>Validates quantity.</li>
     *   <li>Decrements inventory (allocatedQty + onHandQty).</li>
     *   <li>Records an inventory transaction.</li>
     *   <li>Checks whether all picks for the parent order are done.</li>
     *   <li>If all done: transitions order to PICKED, creates packing tasks,
     *       transitions order to PACKING — all within the same transaction.</li>
     * </ul>
     */
    PickingTaskResponseDto completePicking(Long id, CompletePickingRequestDto request);
}
