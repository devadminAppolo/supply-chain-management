package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DriverDto;
import com.supplychain.service.DriverService;
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
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverDto>>> list() {
        List<DriverDto> drivers = driverService.findAll();
        return ResponseEntity.ok(ApiResponse.success(drivers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> save(@RequestBody DriverDto dto) {
        driverService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver saved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverDto> update(@PathVariable Long id, @RequestBody DriverDto dto) {
        return ResponseEntity.ok(driverService.update(id, dto));
    }
}
