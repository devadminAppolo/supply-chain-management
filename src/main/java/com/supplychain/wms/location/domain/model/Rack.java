package com.supplychain.wms.location.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.common.domain.enums.RackType;
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
@Table(name = "racks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Rack extends BaseEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "aisle_id", nullable = false)
    private Long aisleId;

    @Column(name = "rack_code", nullable = false)
    private String rackCode;

    @Column(name = "rack_name", nullable = false)
    private String rackName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rack_type")
    private RackType rackType;

    @Column(name = "levels")
    private Integer levels;

    @Column(name = "max_weight")
    private Double maxWeight;
}

