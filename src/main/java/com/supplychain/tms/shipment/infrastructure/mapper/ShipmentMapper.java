package com.supplychain.tms.shipment.infrastructure.mapper;

import com.supplychain.tms.shipment.application.dto.ShipmentDto;
import com.supplychain.tms.shipment.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDto dto);

    ShipmentDto toDto(Shipment entity);

    List<ShipmentDto> toDtoList(List<Shipment> entities);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ShipmentDto dto, @MappingTarget Shipment entity);
}
