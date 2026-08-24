package com.supplychain.wms.location.presentation.controller;

import com.supplychain.wms.location.application.dto.WarehouseDto;
import com.supplychain.wms.location.application.service.WarehouseService;
import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api/warehouses")
@CrossOrigin
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseDto>> save(@Valid @RequestBody WarehouseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(warehouseService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDto>> update(@PathVariable Long id, @Valid @RequestBody WarehouseDto dto) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WarehouseDto>>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "warehouseName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.findAll(search, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.findDropdown()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        warehouseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        warehouseService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
