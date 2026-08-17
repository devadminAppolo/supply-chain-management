package com.supplychain.entity;

import com.supplychain.enums.BinType;
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
@Table(name = "bins")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Bin extends BaseEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "aisle_id", nullable = false)
    private Long aisleId;

    @Column(name = "rack_id", nullable = false)
    private Long rackId;

    @Column(name = "bin_code", nullable = false)
    private String binCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "bin_type")
    private BinType binType;

    @Column(name = "capacity")
    private Double capacity;

    @Column(name = "capacity_uom")
    private String capacityUom;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "occupied", nullable = false)
    private boolean occupied = false;

    @Column(name = "allow_mixed_sku", nullable = false)
    private boolean allowMixedSku = false;

    @Column(name = "allow_mixed_lot", nullable = false)
    private boolean allowMixedLot = false;

    @Column(name = "barcode")
    private String barcode;
}
