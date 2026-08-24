package com.supplychain.catalog.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.common.domain.enums.TemperatureType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "brand")
    private String brand;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "length")
    private Double length;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Builder.Default
    @Column(name = "expiry_required", nullable = false)
    private boolean expiryRequired = false;

    @Builder.Default
    @Column(name = "serial_tracking", nullable = false)
    private boolean serialTracking = false;

    @Builder.Default
    @Column(name = "lot_tracking", nullable = false)
    private boolean lotTracking = false;

    @Builder.Default
    @Column(name = "hazardous", nullable = false)
    private boolean hazardous = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_type")
    private TemperatureType temperatureType;

    @Column(name = "image_url")
    private String imageUrl;
}

