package com.supplychain.mapper;

import com.supplychain.dto.ZoneDto;
import com.supplychain.entity.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    @Mapping(target = "warehouseId", ignore = true)
    Zone toEntity(ZoneDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    ZoneDto toDto(Zone entity);

    List<ZoneDto> toDtoList(List<Zone> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    void updateEntity(ZoneDto dto, @MappingTarget Zone entity);
}
