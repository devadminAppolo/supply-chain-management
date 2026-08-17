package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.RackDto;
import com.supplychain.service.RackService;
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
@RequestMapping("/api/racks")
@RequiredArgsConstructor
public class RackController {

    private final RackService rackService;

    @PostMapping
    public ResponseEntity<ApiResponse<RackDto>> save(@Valid @RequestBody RackDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(rackService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RackDto>> update(@PathVariable Long id, @Valid @RequestBody RackDto dto) {
        return ResponseEntity.ok(ApiResponse.success(rackService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RackDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rackService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RackDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long aisleId,
            @PageableDefault(size = 20, sort = "rackName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(rackService.findAll(search, warehouseId, zoneId, aisleId, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(
            @RequestParam String aisleId) {
        return ResponseEntity.ok(ApiResponse.success(rackService.findDropdown(aisleId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        rackService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        rackService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
