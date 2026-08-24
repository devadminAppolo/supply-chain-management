package com.supplychain.wms.location.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AisleDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "Zone is required")
    private String zoneId;

    @NotBlank(message = "Aisle code is required")
    private String aisleCode;

    @NotBlank(message = "Aisle name is required")
    private String aisleName;

    private Integer sequence;
    private Double length;
    private Double width;
}
