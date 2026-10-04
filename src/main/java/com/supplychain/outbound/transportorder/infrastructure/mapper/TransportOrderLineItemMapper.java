package com.supplychain.outbound.transportorder.infrastructure.mapper;

import com.supplychain.outbound.transportorder.application.dto.TransportOrderLineItemResponseDto;
import com.supplychain.outbound.transportorder.domain.model.TransportOrderLineItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransportOrderLineItemMapper {

    TransportOrderLineItemResponseDto toDto(TransportOrderLineItem entity);

    List<TransportOrderLineItemResponseDto> toDtoList(List<TransportOrderLineItem> entities);
}
