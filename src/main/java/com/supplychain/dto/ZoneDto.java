package com.supplychain.dto;

import com.supplychain.enums.TemperatureType;
import com.supplychain.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZoneDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "Zone code is required")
    private String zoneCode;

    @NotBlank(message = "Zone name is required")
    private String zoneName;

    private ZoneType zoneType;
    private TemperatureType temperatureType;
    private Integer sequence;
}
