package com.supplychain.outbound.picking.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkAssignPickingRequestDto {

    @NotEmpty(message = "At least one pick task ID is required")
    private List<Long> pickTaskIds;

    @NotNull(message = "Staff ID is required")
    private Long staffId;
}
