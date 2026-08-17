package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.ZoneDto;
import com.supplychain.service.ZoneService;
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
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping
    public ResponseEntity<ApiResponse<ZoneDto>> save(@Valid @RequestBody ZoneDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(zoneService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ZoneDto>> update(@PathVariable Long id, @Valid @RequestBody ZoneDto dto) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ZoneDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ZoneDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @PageableDefault(size = 20, sort = "zoneName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.findAll(search, warehouseId, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(
            @RequestParam String warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.findDropdown(warehouseId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        zoneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        zoneService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
