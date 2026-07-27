package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.LocationDto;
import com.supplychain.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationDto>>> list() {
        List<LocationDto> locations = locationService.findAll();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> save(@RequestBody LocationDto dto) {
        locationService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location saved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> update(@PathVariable Long id, @RequestBody LocationDto dto) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }
}
