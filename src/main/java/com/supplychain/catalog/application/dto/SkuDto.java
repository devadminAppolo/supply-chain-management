package com.supplychain.catalog.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.PackType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuDto extends BaseDto {

    @NotBlank(message = "Product is required")
    private String productId;           // product name

    @NotBlank(message = "SKU code is required")
    private String skuCode;

    private String upc;
    private String ean;
    private PackType packType;
    private String color;
    private String size;
    private Double weight;
}

