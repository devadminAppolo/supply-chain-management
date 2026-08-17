package com.supplychain.mapper;

import com.supplychain.dto.RackDto;
import com.supplychain.entity.Rack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RackMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    Rack toEntity(RackDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    RackDto toDto(Rack entity);

    List<RackDto> toDtoList(List<Rack> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    void updateEntity(RackDto dto, @MappingTarget Rack entity);
}
