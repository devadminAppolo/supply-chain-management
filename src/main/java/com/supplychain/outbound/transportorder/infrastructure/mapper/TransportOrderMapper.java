package com.supplychain.outbound.transportorder.infrastructure.mapper;

import com.supplychain.outbound.transportorder.application.dto.TransportOrderOutboundOrderResponseDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderResponseDto;
import com.supplychain.outbound.transportorder.domain.model.TransportOrder;
import com.supplychain.outbound.transportorder.domain.model.TransportOrderOutboundOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TransportOrderLineItemMapper.class})
public interface TransportOrderMapper {

    /**
     * Maps a TransportOrder entity to a response DTO.
     * Line items are delegated to TransportOrderLineItemMapper via the {@code uses} attribute.
     * Association objects are mapped by the {@link #toAssocDto} method defined below.
     */
    @Mapping(target = "lineItems", source = "lineItems")
    @Mapping(target = "outboundOrders", source = "outboundOrderAssociations")
    TransportOrderResponseDto toDto(TransportOrder entity);

    List<TransportOrderResponseDto> toDtoList(List<TransportOrder> entities);

    /**
     * Maps a single TransportOrderOutboundOrder association entity to its response DTO.
     * MapStruct uses this method when mapping the {@code outboundOrderAssociations} list.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "outboundOrderId", source = "outboundOrderId")
    TransportOrderOutboundOrderResponseDto toAssocDto(TransportOrderOutboundOrder entity);
}
