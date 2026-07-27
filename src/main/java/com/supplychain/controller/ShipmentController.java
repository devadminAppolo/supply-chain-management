package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.ShipmentDto;
import com.supplychain.service.ShipmentService;
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
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

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
}
