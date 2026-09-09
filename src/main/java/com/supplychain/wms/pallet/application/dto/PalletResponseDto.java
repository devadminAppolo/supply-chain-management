package com.supplychain.wms.pallet.application.dto;

import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalletResponseDto {

    private Long id;
    private Long orgId;
    private Long dcId;
    private String palletNumber;
    private String lpnNumber;
    private PalletType palletType;
    private PalletMaterial palletMaterial;
    private PalletStatus palletStatus;
    private String status;
    private BigDecimal maxWeight;
    private String weightUom;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String dimensionUom;
    private BigDecimal tareWeight;
    private String tareWeightUom;
    private String barcode;
    private String description;
    private String notes;
    private boolean active;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private Long version;
}
