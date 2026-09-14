package com.supplychain.outbound.outboundorder.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderCreateRequestDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderResponseDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderUpdateRequestDto;
import com.supplychain.outbound.outboundorder.application.service.OutboundOrderService;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/outbound-orders")
@RequiredArgsConstructor
public class OutboundOrderController {

    private final OutboundOrderService service;

    // -------------------------------------------------------
    // POST /api/outbound-orders
    // Create a new Outbound Order (starts as DRAFT)
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<OutboundOrderResponseDto>> create(
            @Valid @RequestBody OutboundOrderCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createOutboundOrder(request)));
    }

    // -------------------------------------------------------
    // PUT /api/outbound-orders/{id}
    // Update an existing order (only DRAFT or PENDING)
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OutboundOrderResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody OutboundOrderUpdateRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOutboundOrder(id, request)));
    }

    // -------------------------------------------------------
    // GET /api/outbound-orders/{id}
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutboundOrderResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    // -------------------------------------------------------
    // GET /api/outbound-orders
    // Paginated list with optional filters
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OutboundOrderResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OutboundOrderStatus orderStatus,
            @RequestParam(required = false) OutboundOrderType orderType,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                service.findAll(search, orderStatus, orderType, customerCode, startDate, endDate, pageable)));
    }

    // -------------------------------------------------------
    // GET /api/outbound-orders/dropdown
    // -------------------------------------------------------
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(service.findDropdown()));
    }

    // -------------------------------------------------------
    // POST /api/outbound-orders/{id}/confirm
    // Transitions DRAFT/PENDING → CONFIRMED (no inventory impact)
    // -------------------------------------------------------
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OutboundOrderResponseDto>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.confirmOutboundOrder(id)));
    }

    // -------------------------------------------------------
    // PATCH /api/outbound-orders/{id}/cancel
    // Transitions DRAFT/PENDING/CONFIRMED → CANCELLED
    // -------------------------------------------------------
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OutboundOrderResponseDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancelOutboundOrder(id)));
    }

    // -------------------------------------------------------
    // DELETE /api/outbound-orders/{id}
    // Soft-delete (only DRAFT orders)
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // DELETE /api/outbound-orders/batch
    // Bulk soft-delete (each must be DRAFT)
    // -------------------------------------------------------
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        service.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
