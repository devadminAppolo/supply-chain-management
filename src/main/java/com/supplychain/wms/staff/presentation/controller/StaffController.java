package com.supplychain.wms.staff.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.staff.application.dto.StaffDto;
import com.supplychain.wms.staff.application.service.StaffService;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long dcId) {
        return ResponseEntity.ok(ApiResponse.success(staffService.findDropdown(orgId, dcId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StaffDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long dcId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(staffService.findAll(search, status, orgId, dcId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffDto>> save(@Valid @RequestBody StaffDto dto) {
        StaffDto created = staffService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff member created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> update(@PathVariable Long id, @Valid @RequestBody StaffDto dto) {
        StaffDto updated = staffService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Staff member updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        staffService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Staff member deleted successfully", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteAllByIds(@RequestBody List<Long> ids) {
        staffService.deleteAllByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Selected staff members deleted successfully", null));
    }
}
