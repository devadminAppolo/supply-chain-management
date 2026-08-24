package com.supplychain.catalog.infrastructure.mapper;

import com.supplychain.catalog.application.dto.CategoryDto;
import com.supplychain.catalog.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentCategoryId", ignore = true)
    Category toEntity(CategoryDto dto);

    @Mapping(target = "parentCategoryId", ignore = true)
    CategoryDto toDto(Category entity);

    List<CategoryDto> toDtoList(List<Category> entities);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "createdDate",      ignore = true)
    @Mapping(target = "modifiedBy",       ignore = true)
    @Mapping(target = "modifiedDate",     ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "parentCategoryId", ignore = true)
    void updateEntity(CategoryDto dto, @MappingTarget Category entity);
}
