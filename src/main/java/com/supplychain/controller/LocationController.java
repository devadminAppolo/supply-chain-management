package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.LocationDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.enums.LocationType;
import com.supplychain.service.LocationService;
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
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LocationDto>> save(@Valid @RequestBody LocationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(locationService.save(dto)));
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
