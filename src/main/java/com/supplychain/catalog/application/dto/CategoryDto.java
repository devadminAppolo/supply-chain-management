package com.supplychain.catalog.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto extends BaseDto {

    @NotBlank(message = "Category code is required")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String parentCategoryId;    // optional – name of the parent category
    private String description;
    private Integer displaySequence;
}
