package com.supplychain.dto;

import com.supplychain.enums.TemperatureType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto extends BaseDto {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String shortDescription;
    private String longDescription;
    private String categoryId;          // optional – category name
    private String uomId;               // optional – UOM name
    private String barcode;
    private String manufacturer;
    private String brand;
    private String countryOfOrigin;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Double volume;
    private Integer shelfLifeDays;
    private boolean expiryRequired;
    private boolean serialTracking;
    private boolean lotTracking;
    private boolean hazardous;
    private TemperatureType temperatureType;
    private String imageUrl;
}
