package com.supplychain.wms.putaway.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.putaway.application.dto.AssignStaffRequestDto;
import com.supplychain.wms.putaway.application.dto.CompletePutawayRequestDto;
import com.supplychain.wms.putaway.application.dto.PutawayTaskDto;
import com.supplychain.wms.putaway.application.service.PutawayService;
import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/putaway-tasks")
@RequiredArgsConstructor
public class PutawayController {

    private final PutawayService putawayService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PutawayTaskDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PutawayStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String staffId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(putawayService.findAll(search, status, warehouseId, staffId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PutawayTaskDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(putawayService.findById(id)));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<PutawayTaskDto>> assignStaff(
            @PathVariable Long id,
            @Valid @RequestBody AssignStaffRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Staff assigned to putaway task successfully", putawayService.assignStaff(id, request)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<PutawayTaskDto>> startPutaway(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Putaway task started successfully", putawayService.startPutaway(id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PutawayTaskDto>> completePutaway(
            @PathVariable Long id,
            @Valid @RequestBody CompletePutawayRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Putaway task completed successfully and inventory relocated", putawayService.completePutaway(id, request)));
    }
}
