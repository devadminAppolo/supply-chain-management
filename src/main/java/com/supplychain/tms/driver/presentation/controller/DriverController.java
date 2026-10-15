package com.supplychain.tms.driver.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.tms.driver.application.dto.DriverDto;
import com.supplychain.tms.driver.application.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverDto>>> list() {
        List<DriverDto> drivers = driverService.findAll();
        return ResponseEntity.ok(ApiResponse.success(drivers));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(driverService.findDropdown()));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        driverService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        driverService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
