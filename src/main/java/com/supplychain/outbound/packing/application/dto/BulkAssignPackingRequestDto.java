package com.supplychain.outbound.packing.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkAssignPackingRequestDto {

    @NotEmpty(message = "At least one packing task ID is required")
    private List<Long> packingTaskIds;

    @NotNull(message = "Staff ID is required")
    private Long staffId;
}
