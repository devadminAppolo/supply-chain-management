package com.supplychain.wms.inventorytransaction.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.wms.inventorytransaction.application.dto.InventoryTransactionDto;
import com.supplychain.wms.inventorytransaction.application.service.InventoryTransactionService;
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
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryTransactionDto>> save(
            @Valid @RequestBody InventoryTransactionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryTransactionService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryTransactionDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryTransactionDto dto) {
        return ResponseEntity.ok(ApiResponse.success(inventoryTransactionService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryTransactionDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryTransactionService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long inventoryId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) TransactionType transactionType,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryTransactionService.findAll(search, warehouseId, inventoryId, skuId, transactionType, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        inventoryTransactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        inventoryTransactionService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
