package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.UomDto;
import com.supplychain.service.UomService;
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
@RequestMapping("/api/uoms")
@RequiredArgsConstructor
public class UomController {

    private final UomService uomService;

    @PostMapping
    public ResponseEntity<ApiResponse<UomDto>> save(@Valid @RequestBody UomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(uomService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UomDto>> update(@PathVariable Long id, @Valid @RequestBody UomDto dto) {
        return ResponseEntity.ok(ApiResponse.success(uomService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UomDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UomDto>>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "uomName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findAll(search, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(uomService.findDropdown()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        uomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        uomService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
