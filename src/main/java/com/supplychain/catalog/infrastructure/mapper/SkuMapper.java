package com.supplychain.catalog.infrastructure.mapper;

import com.supplychain.catalog.application.dto.SkuDto;
import com.supplychain.catalog.domain.model.Sku;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkuMapper {

    @Mapping(target = "productId", ignore = true)
    Sku toEntity(SkuDto dto);

    @Mapping(target = "productId", ignore = true)
    SkuDto toDto(Sku entity);

    List<SkuDto> toDtoList(List<Sku> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "productId",   ignore = true)
    void updateEntity(SkuDto dto, @MappingTarget Sku entity);
}
