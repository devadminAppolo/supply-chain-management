package com.supplychain.wms.inventoryadjustment.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.AdjustmentType;
import com.supplychain.wms.inventoryadjustment.application.dto.InventoryAdjustmentDto;
import com.supplychain.wms.inventoryadjustment.application.service.InventoryAdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-adjustments")
@RequiredArgsConstructor
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService inventoryAdjustmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryAdjustmentDto>> save(
            @Valid @RequestBody InventoryAdjustmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryAdjustmentService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryAdjustmentDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryAdjustmentDto dto) {
        return ResponseEntity.ok(ApiResponse.success(inventoryAdjustmentService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryAdjustmentDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryAdjustmentService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryAdjustmentDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) AdjustmentType adjustmentType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryAdjustmentService.findAll(search, warehouseId, skuId, binId, adjustmentType, status, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        inventoryAdjustmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        inventoryAdjustmentService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
