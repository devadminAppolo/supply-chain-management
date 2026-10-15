package com.supplychain.customer.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.customer.application.dto.CustomerRequestDto;
import com.supplychain.customer.application.dto.CustomerResponseDto;
import com.supplychain.customer.application.dto.CustomerUpdateDto;
import com.supplychain.customer.application.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDto>> create(
            @Valid @RequestBody CustomerRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(customerService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "customerName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll(search, pageable)));
    }

    /** Returns only ACTIVE customers – used when selecting a customer for an Outbound Order. */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CustomerResponseDto>>> findActive() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findActive()));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findDropdown()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        customerService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
