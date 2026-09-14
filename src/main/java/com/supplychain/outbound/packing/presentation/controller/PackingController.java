package com.supplychain.outbound.packing.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.packing.application.dto.BulkAssignPackingRequestDto;
import com.supplychain.outbound.packing.application.dto.CompletePackingRequestDto;
import com.supplychain.outbound.packing.application.dto.PackingTaskResponseDto;
import com.supplychain.outbound.packing.application.service.PackingService;
import com.supplychain.outbound.packing.domain.enums.PackingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PackingTaskResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PackingStatus status,
            @RequestParam(required = false) Long outboundOrderId,
            @RequestParam(required = false) String staffId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                packingService.findAll(search, status, outboundOrderId, staffId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackingTaskResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(packingService.findById(id)));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<List<PackingTaskResponseDto>>> bulkAssign(
            @Valid @RequestBody BulkAssignPackingRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Packing tasks assigned successfully", packingService.bulkAssign(request)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<PackingTaskResponseDto>> startPacking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Packing task started successfully", packingService.startPacking(id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PackingTaskResponseDto>> completePacking(
            @PathVariable Long id,
            @Valid @RequestBody CompletePackingRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Packing task completed successfully", packingService.completePacking(id, request)));
    }
}
