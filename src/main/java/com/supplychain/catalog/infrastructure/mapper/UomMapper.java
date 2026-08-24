package com.supplychain.catalog.infrastructure.mapper;

import com.supplychain.catalog.application.dto.UomDto;
import com.supplychain.catalog.domain.model.Uom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UomMapper {

    Uom toEntity(UomDto dto);

    UomDto toDto(Uom entity);

    List<UomDto> toDtoList(List<Uom> entities);

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "createdDate",   ignore = true)
    @Mapping(target = "modifiedBy",    ignore = true)
    @Mapping(target = "modifiedDate",  ignore = true)
    @Mapping(target = "deleted",       ignore = true)
    void updateEntity(UomDto dto, @MappingTarget Uom entity);
}
