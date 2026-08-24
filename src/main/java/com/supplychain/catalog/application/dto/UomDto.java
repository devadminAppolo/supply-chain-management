package com.supplychain.catalog.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.UomType;
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

