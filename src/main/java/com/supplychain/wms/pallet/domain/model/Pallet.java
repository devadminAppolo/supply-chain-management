package com.supplychain.wms.pallet.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "pallets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Pallet extends BaseEntity {

    @Column(name = "pallet_number", nullable = false, length = 50)
    private String palletNumber;

    @Column(name = "lpn_number", nullable = false, length = 50)
    private String lpnNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "pallet_type", nullable = false, length = 50)
    private PalletType palletType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pallet_material", nullable = false, length = 50)
    private PalletMaterial palletMaterial;

    @Enumerated(EnumType.STRING)
    @Column(name = "pallet_status", nullable = false, length = 50)
    private PalletStatus palletStatus;

    @Column(name = "max_weight", precision = 19, scale = 4)
    private BigDecimal maxWeight;

    @Column(name = "weight_uom", length = 50)
    private String weightUom;

    @Column(name = "length", precision = 19, scale = 4)
    private BigDecimal length;

    @Column(name = "width", precision = 19, scale = 4)
    private BigDecimal width;

    @Column(name = "height", precision = 19, scale = 4)
    private BigDecimal height;

    @Column(name = "dimension_uom", length = 50)
    private String dimensionUom;

    @Column(name = "tare_weight", precision = 19, scale = 4)
    private BigDecimal tareWeight;

    @Column(name = "tare_weight_uom", length = 50)
    private String tareWeightUom;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
