package com.supplychain.mapper;

import com.supplychain.dto.WarehouseDto;
import com.supplychain.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseDto dto);

    WarehouseDto toDto(Warehouse entity);

    List<WarehouseDto> toDtoList(List<Warehouse> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(WarehouseDto dto, @MappingTarget Warehouse entity);
}
