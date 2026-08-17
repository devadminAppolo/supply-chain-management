package com.supplychain.dto;

import com.supplychain.enums.UomType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UomDto extends BaseDto {

    @NotBlank(message = "UOM code is required")
    private String uomCode;

    @NotBlank(message = "UOM name is required")
    private String uomName;

    private String symbol;
    private UomType uomType;
    private boolean baseUom;
    private Double conversionFactor;
}
