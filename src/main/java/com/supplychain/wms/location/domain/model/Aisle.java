package com.supplychain.wms.location.domain.model;

import com.supplychain.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "aisles")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Aisle extends BaseEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "aisle_code", nullable = false)
    private String aisleCode;

    @Column(name = "aisle_name", nullable = false)
    private String aisleName;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "length")
    private Double length;

    @Column(name = "width")
    private Double width;
}
