package com.supplychain.wms.dashboard.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.wms.dashboard.application.dto.DashboardDto;
import com.supplychain.wms.dashboard.application.service.DashboardService;
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

    @GetMapping("/inventory-summary")
    public ResponseEntity<ApiResponse<DashboardDto>> getInventorySummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInventorySummary()));
    }
}
