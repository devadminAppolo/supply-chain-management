package com.supplychain.mapper;

import com.supplychain.dto.BinDto;
import com.supplychain.entity.Bin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BinMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    @Mapping(target = "rackId",      ignore = true)
    Bin toEntity(BinDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    @Mapping(target = "rackId",      ignore = true)
    BinDto toDto(Bin entity);

    List<BinDto> toDtoList(List<Bin> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    @Mapping(target = "aisleId",     ignore = true)
    @Mapping(target = "rackId",      ignore = true)
    void updateEntity(BinDto dto, @MappingTarget Bin entity);
}
