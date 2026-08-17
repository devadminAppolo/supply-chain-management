package com.supplychain.controller;

import com.supplychain.dto.ApiResponse;
import com.supplychain.dto.DashboardDto;
import com.supplychain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Returns all data required by the WMS inventory dashboard in a single response:
     * KPI summary, inventory by warehouse, inventory by status, recent transactions.
     */
    @GetMapping("/inventory-summary")
    public ResponseEntity<ApiResponse<DashboardDto>> getInventorySummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInventorySummary()));
    }
}
