package com.supplychain.entity;

import com.supplychain.enums.UomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "uoms")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Uom extends BaseEntity {

    @Column(name = "uom_code", nullable = false)
    private String uomCode;

    @Column(name = "uom_name", nullable = false)
    private String uomName;

    @Column(name = "symbol")
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "uom_type")
    private UomType uomType;

    @Column(name = "base_uom", nullable = false)
    private boolean baseUom = false;

    @Column(name = "conversion_factor")
    private Double conversionFactor;
}
