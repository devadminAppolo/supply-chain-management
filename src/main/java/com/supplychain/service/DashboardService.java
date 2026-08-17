package com.supplychain.service;

import com.supplychain.dto.DashboardDto;

public interface DashboardService {

    /** Returns the consolidated inventory dashboard payload. Read-only, no side effects. */
    DashboardDto getInventorySummary();
}
