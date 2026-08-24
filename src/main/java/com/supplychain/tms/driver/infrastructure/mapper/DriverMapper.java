package com.supplychain.tms.driver.infrastructure.mapper;

import com.supplychain.tms.driver.application.dto.DriverDto;
import com.supplychain.tms.driver.domain.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DriverMapper {

    Driver toEntity(DriverDto dto);

    DriverDto toDto(Driver entity);

    List<DriverDto> toDtoList(List<Driver> entities);

    @Mapping(target = "id", ignore = true)
    void updateEntity(DriverDto dto, @MappingTarget Driver entity);
}
