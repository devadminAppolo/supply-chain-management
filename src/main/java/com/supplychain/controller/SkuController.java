package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.SkuDto;
import com.supplychain.service.SkuService;
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
@RequestMapping("/api/skus")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @PostMapping
    public ResponseEntity<ApiResponse<SkuDto>> save(@Valid @RequestBody SkuDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(skuService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkuDto>> update(@PathVariable Long id, @Valid @RequestBody SkuDto dto) {
        return ResponseEntity.ok(ApiResponse.success(skuService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkuDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skuService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SkuDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long productId,
            @PageableDefault(size = 20, sort = "skuCode") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(skuService.findAll(search, productId, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(ApiResponse.success(skuService.findDropdown(productId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        skuService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        skuService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
