package com.supplychain.mapper;

import com.supplychain.dto.ProductDto;
import com.supplychain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "uomId",      ignore = true)
    Product toEntity(ProductDto dto);

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "uomId",      ignore = true)
    ProductDto toDto(Product entity);

    List<ProductDto> toDtoList(List<Product> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "categoryId",  ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    void updateEntity(ProductDto dto, @MappingTarget Product entity);
}
