package com.supplychain.tms.shipment.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.tms.shipment.application.dto.ShipmentDto;
import com.supplychain.tms.shipment.application.service.ShipmentService;
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
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentDto>>> list() {
        List<ShipmentDto> shipments = shipmentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(shipments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> save(@RequestBody ShipmentDto dto) {
        shipmentService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment saved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentDto> update(@PathVariable Long id, @RequestBody ShipmentDto dto) {
        return ResponseEntity.ok(shipmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        shipmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        shipmentService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
