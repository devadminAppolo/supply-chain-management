package com.supplychain.outbound.outboundorder.infrastructure.mapper;

import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderResponseDto;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OutboundOrderLineMapper.class})
public interface OutboundOrderMapper {

    /**
     * Maps an OutboundOrder entity (with its lines collection already populated)
     * to a full response DTO. MapStruct will delegate line mapping to
     * OutboundOrderLineMapper via the `uses` attribute.
     */
    OutboundOrderResponseDto toDto(OutboundOrder entity);

    List<OutboundOrderResponseDto> toDtoList(List<OutboundOrder> entities);

    /**
     * Applies mutable header fields from an update request onto an existing
     * managed entity.  Business-controlled fields (orderNumber, orderStatus),
     * generated/calculated fields, child lines, and audit fields are all
     * managed by the service.
     */
    @Mapping(target = "id",                      ignore = true)
    @Mapping(target = "orderNumber",             ignore = true)
    @Mapping(target = "orderStatus",             ignore = true)
    @Mapping(target = "lines",                   ignore = true)
    @Mapping(target = "totalAmount",             ignore = true)
    @Mapping(target = "originLocationCode",      ignore = true)
    @Mapping(target = "originLocationName",      ignore = true)
    @Mapping(target = "destinationLocationCode", ignore = true)
    @Mapping(target = "destinationLocationName", ignore = true)
    @Mapping(target = "createdBy",               ignore = true)
    @Mapping(target = "createdDate",             ignore = true)
    @Mapping(target = "modifiedBy",              ignore = true)
    @Mapping(target = "modifiedDate",            ignore = true)
    @Mapping(target = "deleted",                 ignore = true)
    @Mapping(target = "orgId",                   ignore = true)
    @Mapping(target = "dcId",                    ignore = true)
    @Mapping(target = "status",                  ignore = true)
    @Mapping(target = "remarks",                 ignore = true)
    void updateEntity(com.supplychain.outbound.outboundorder.application.dto.OutboundOrderUpdateRequestDto dto,
                      @MappingTarget OutboundOrder entity);
}
