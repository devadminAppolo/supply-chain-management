package com.supplychain.wms.location.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.RackType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RackDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "Zone is required")
    private String zoneId;

    @NotBlank(message = "Aisle is required")
    private String aisleId;

    @NotBlank(message = "Rack code is required")
    private String rackCode;

    @NotBlank(message = "Rack name is required")
    private String rackName;

    private RackType rackType;
    private Integer levels;
    private Double maxWeight;
}

