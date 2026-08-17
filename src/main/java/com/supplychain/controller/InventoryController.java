package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.InventoryDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.enums.InventoryStatus;
import com.supplychain.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDto>> save(@Valid @RequestBody InventoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> update(@PathVariable Long id,
                                                            @Valid @RequestBody InventoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long binId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) InventoryStatus inventoryStatus,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.findAll(search, warehouseId, binId, productId, skuId, inventoryStatus, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        inventoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        inventoryService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
