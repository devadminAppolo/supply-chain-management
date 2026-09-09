package com.supplychain.wms.receiving.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.receiving.application.dto.ReceivingCreateRequestDto;
import com.supplychain.wms.receiving.application.dto.ReceivingResponseDto;
import com.supplychain.wms.receiving.application.service.ReceivingService;
import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receivings")
@RequiredArgsConstructor
public class ReceivingController {

    private final ReceivingService receivingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReceivingResponseDto>> createReceiving(@Valid @RequestBody ReceivingCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receiving created successfully", receivingService.createReceiving(request)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ReceivingResponseDto>> completeReceiving(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Receiving completed and inventory received to staging", receivingService.completeReceiving(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceivingResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(receivingService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReceivingResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ReceivingStatus status,
            @RequestParam(required = false) Long warehouseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(receivingService.findAll(search, status, warehouseId, pageable)));
    }
}
