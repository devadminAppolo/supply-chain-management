package com.supplychain.outbound.picking.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.picking.application.dto.BulkAssignPickingRequestDto;
import com.supplychain.outbound.picking.application.dto.CompletePickingRequestDto;
import com.supplychain.outbound.picking.application.dto.PickingTaskResponseDto;
import com.supplychain.outbound.picking.application.service.PickingService;
import com.supplychain.outbound.picking.domain.enums.PickingStatus;
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
@RequestMapping("/api/picking")
@RequiredArgsConstructor
public class PickingController {

    private final PickingService pickingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PickingTaskResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PickingStatus status,
            @RequestParam(required = false) Long outboundOrderId,
            @RequestParam(required = false) String staffId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                pickingService.findAll(search, status, outboundOrderId, staffId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PickingTaskResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pickingService.findById(id)));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<List<PickingTaskResponseDto>>> bulkAssign(
            @Valid @RequestBody BulkAssignPickingRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pick tasks assigned successfully", pickingService.bulkAssign(request)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<PickingTaskResponseDto>> startPicking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pick task started successfully", pickingService.startPicking(id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PickingTaskResponseDto>> completePicking(
            @PathVariable Long id,
            @Valid @RequestBody CompletePickingRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pick task completed successfully", pickingService.completePicking(id, request)));
    }
}
