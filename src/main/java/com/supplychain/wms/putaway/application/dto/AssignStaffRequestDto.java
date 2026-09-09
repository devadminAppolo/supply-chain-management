package com.supplychain.wms.putaway.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignStaffRequestDto {

    @NotBlank(message = "Staff ID is required")
    private String assignedStaffId;

    private String assignedStaffName;
}
