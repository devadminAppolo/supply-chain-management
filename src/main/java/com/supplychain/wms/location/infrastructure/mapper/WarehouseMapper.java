package com.supplychain.wms.location.infrastructure.mapper;

import com.supplychain.wms.location.application.dto.WarehouseDto;
import com.supplychain.wms.location.domain.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseDto dto);

    WarehouseDto toDto(Warehouse entity);

    List<WarehouseDto> toDtoList(List<Warehouse> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    void updateEntity(WarehouseDto dto, @MappingTarget Warehouse entity);
}
