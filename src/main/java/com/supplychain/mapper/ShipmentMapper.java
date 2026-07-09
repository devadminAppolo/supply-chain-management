package com.supplychain.mapper;

import com.supplychain.dto.ShipmentDto;
import com.supplychain.entity.Shipment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDto dto);

    ShipmentDto toDto(Shipment entity);

    List<ShipmentDto> toDtoList(List<Shipment> entities);
}