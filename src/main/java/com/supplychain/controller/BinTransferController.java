package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.BinTransferDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.service.BinTransferService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bin-transfers")
@RequiredArgsConstructor
public class BinTransferController {

    private final BinTransferService binTransferService;

    /**
     * Executes a bin-to-bin inventory transfer atomically.
     * Validates source availability, deducts from source, credits destination,
     * and creates an audit transaction — all in one database transaction.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BinTransferDto>> transfer(
            @Valid @RequestBody BinTransferDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(binTransferService.transfer(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BinTransferDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(binTransferService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BinTransferDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long sourceWarehouseId,
            @RequestParam(required = false) Long sourceBinId,
            @RequestParam(required = false) Long skuId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                binTransferService.findAll(search, sourceWarehouseId, sourceBinId, skuId, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        binTransferService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        binTransferService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
