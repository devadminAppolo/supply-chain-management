package com.supplychain.wms.location.presentation.controller;

import com.supplychain.wms.location.application.dto.BinDto;
import com.supplychain.wms.location.application.service.BinService;
import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
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
@RequestMapping("/api/bins")
@RequiredArgsConstructor
public class BinController {

    private final BinService binService;

    @PostMapping
    public ResponseEntity<ApiResponse<BinDto>> save(@Valid @RequestBody BinDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(binService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BinDto>> update(@PathVariable Long id, @Valid @RequestBody BinDto dto) {
        return ResponseEntity.ok(ApiResponse.success(binService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BinDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(binService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BinDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long aisleId,
            @RequestParam(required = false) Long rackId,
            @PageableDefault(size = 20, sort = "binCode") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(binService.findAll(search, warehouseId, zoneId, aisleId, rackId, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(@RequestParam String rackId) {
        return ResponseEntity.ok(ApiResponse.success(binService.findDropdown(rackId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        binService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        binService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
