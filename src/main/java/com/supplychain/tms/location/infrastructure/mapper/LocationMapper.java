package com.supplychain.tms.location.infrastructure.mapper;

import com.supplychain.tms.location.application.dto.LocationDto;
import com.supplychain.tms.location.domain.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);

    List<LocationDto> toDtoList(List<Location> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    void updateEntity(LocationDto dto, @MappingTarget Location entity);
}
