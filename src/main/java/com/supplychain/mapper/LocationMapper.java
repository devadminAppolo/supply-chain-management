package com.supplychain.mapper;

import com.supplychain.dto.LocationDto;
import com.supplychain.entity.Location;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);

    List<LocationDto> toDtoList(List<Location> entities);
}
