package com.supplychain.entity;

import com.supplychain.enums.TemperatureType;
import com.supplychain.enums.ZoneType;
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
@Table(name = "zones")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Zone extends BaseEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "zone_code", nullable = false)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type")
    private ZoneType zoneType;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_type")
    private TemperatureType temperatureType;

    @Column(name = "sequence")
    private Integer sequence;
}
