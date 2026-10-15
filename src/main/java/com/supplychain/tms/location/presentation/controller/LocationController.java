package com.supplychain.tms.location.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.LocationType;
import com.supplychain.tms.location.application.dto.LocationDto;
import com.supplychain.tms.location.application.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

@Validated
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LocationDto>> save(@Valid @RequestBody LocationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(locationService.save(dto)));
    }

    @PostMapping("/create-all")
    public ResponseEntity<ApiResponse<List<LocationDto>>> saveAll(
            @RequestBody List<@Valid LocationDto> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(locationService.saveAll(dtos)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDto>> update(@PathVariable Long id,
                                                           @Valid @RequestBody LocationDto dto) {
        return ResponseEntity.ok(ApiResponse.success(locationService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(locationService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LocationDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocationType locationType,
            @PageableDefault(size = 20, sort = "locationName") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                locationService.findAll(search, locationType, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(locationService.findDropdown()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        locationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        locationService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
