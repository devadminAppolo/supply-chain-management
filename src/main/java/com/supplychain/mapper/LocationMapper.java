package com.supplychain.mapper;

import com.supplychain.dto.LocationDto;
import com.supplychain.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);

    List<LocationDto> toDtoList(List<Location> entities);

    @Mapping(target = "id", ignore = true)
    void updateEntity(LocationDto dto, @MappingTarget Location entity);
}
