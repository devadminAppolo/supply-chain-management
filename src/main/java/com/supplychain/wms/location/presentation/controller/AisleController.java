package com.supplychain.wms.location.presentation.controller;

import com.supplychain.wms.location.application.dto.AisleDto;
import com.supplychain.wms.location.application.service.AisleService;
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
@RequestMapping("/api/aisles")
@RequiredArgsConstructor
public class AisleController {

    private final AisleService aisleService;

    @PostMapping
    public ResponseEntity<ApiResponse<AisleDto>> save(@Valid @RequestBody AisleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(aisleService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AisleDto>> update(@PathVariable Long id, @Valid @RequestBody AisleDto dto) {
        return ResponseEntity.ok(ApiResponse.success(aisleService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AisleDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aisleService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AisleDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @PageableDefault(size = 20, sort = "aisleName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(aisleService.findAll(search, warehouseId, zoneId, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(@RequestParam String zoneId) {
        return ResponseEntity.ok(ApiResponse.success(aisleService.findDropdown(zoneId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        aisleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        aisleService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
