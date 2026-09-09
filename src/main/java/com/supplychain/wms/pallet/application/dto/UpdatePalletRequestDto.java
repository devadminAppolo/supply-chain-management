package com.supplychain.wms.pallet.application.dto;

import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePalletRequestDto {

    private Long orgId;

    private Long dcId;

    @NotNull(message = "Pallet type is required")
    private PalletType palletType;

    @NotNull(message = "Pallet material is required")
    private PalletMaterial palletMaterial;

    @PositiveOrZero(message = "Max weight must be greater than or equal to 0")
    private BigDecimal maxWeight;

    @Size(max = 50, message = "Weight UOM must not exceed 50 characters")
    private String weightUom;

    @PositiveOrZero(message = "Length must be greater than or equal to 0")
    private BigDecimal length;

    @PositiveOrZero(message = "Width must be greater than or equal to 0")
    private BigDecimal width;

    @PositiveOrZero(message = "Height must be greater than or equal to 0")
    private BigDecimal height;

    @Size(max = 50, message = "Dimension UOM must not exceed 50 characters")
    private String dimensionUom;

    @PositiveOrZero(message = "Tare weight must be greater than or equal to 0")
    private BigDecimal tareWeight;

    @Size(max = 50, message = "Tare weight UOM must not exceed 50 characters")
    private String tareWeightUom;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String notes;

    private Boolean active;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
