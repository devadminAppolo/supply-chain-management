package com.supplychain.dto;

import com.supplychain.enums.BinType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "Zone is required")
    private String zoneId;

    @NotBlank(message = "Aisle is required")
    private String aisleId;

    @NotBlank(message = "Rack is required")
    private String rackId;

    @NotBlank(message = "Bin code is required")
    private String binCode;

    private BinType binType;
    private Double capacity;
    private String capacityUom;
    private Double maxWeight;
    private boolean occupied;
    private boolean allowMixedSku;
    private boolean allowMixedLot;
    private String barcode;
}
