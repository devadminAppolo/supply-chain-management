package com.supplychain.outbound.transportorder.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderCreateRequestDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderResponseDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderUpdateRequestDto;
import com.supplychain.outbound.transportorder.application.service.TransportOrderService;
import com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/transport-orders")
@RequiredArgsConstructor
public class TransportOrderController {

    private final TransportOrderService service;

    // -------------------------------------------------------
    // POST /api/transport-orders
    // Create a new Transport Order from READY_TO_SHIP Outbound Orders
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<TransportOrderResponseDto>> create(
            @Valid @RequestBody TransportOrderCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createTransportOrder(request)));
    }

    // -------------------------------------------------------
    // GET /api/transport-orders/{id}
    // Retrieve a Transport Order by primary key
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportOrderResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    // -------------------------------------------------------
    // GET /api/transport-orders
    // Paginated list with optional filters
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransportOrderResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransportOrderStatus toStatus,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "pickUpFrom", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                service.findAll(search, toStatus, mode, startDate, endDate, pageable)));
    }

    // -------------------------------------------------------
    // PUT /api/transport-orders/{id}
    // Update a Transport Order (only DRAFT or PENDING)
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportOrderResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransportOrderUpdateRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTransportOrder(id, request)));
    }

    // -------------------------------------------------------
    // PATCH /api/transport-orders/{id}/cancel
    // Cancel a Transport Order (only DRAFT or PENDING)
    // -------------------------------------------------------
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TransportOrderResponseDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancelTransportOrder(id)));
    }
}
