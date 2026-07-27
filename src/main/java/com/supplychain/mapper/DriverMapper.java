package com.supplychain.mapper;

import com.supplychain.dto.DriverDto;
import com.supplychain.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    Driver toEntity(DriverDto dto);

    DriverDto toDto(Driver entity);

    List<DriverDto> toDtoList(List<Driver> entities);

    @Mapping(target = "id", ignore = true)
    void updateEntity(DriverDto dto, @MappingTarget Driver entity);
}
