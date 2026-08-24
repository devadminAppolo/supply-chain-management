package com.supplychain.wms.location.infrastructure.mapper;

import com.supplychain.wms.location.application.dto.AisleDto;
import com.supplychain.wms.location.domain.model.Aisle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AisleMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    Aisle toEntity(AisleDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    AisleDto toDto(Aisle entity);

    List<AisleDto> toDtoList(List<Aisle> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "zoneId",      ignore = true)
    void updateEntity(AisleDto dto, @MappingTarget Aisle entity);
}
