package com.supplychain.outbound.outboundorder.infrastructure.mapper;

import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderLineResponseDto;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OutboundOrderLineMapper {

    OutboundOrderLineResponseDto toDto(OutboundOrderLine entity);

    List<OutboundOrderLineResponseDto> toDtoList(List<OutboundOrderLine> entities);

    /**
     * Applies updated scalar fields from a line update request onto an existing
     * managed entity. SKU snapshots (skuCode, skuName), calculated fields
     * (totalPrice, totalWeight, totalVolume), line number, parent reference, and
     * audit fields are all managed by the service and are therefore ignored here.
     */
    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "outboundOrder", ignore = true)
    @Mapping(target = "lineNumber",    ignore = true)
    @Mapping(target = "skuCode",       ignore = true)
    @Mapping(target = "skuName",       ignore = true)
    @Mapping(target = "totalPrice",    ignore = true)
    @Mapping(target = "totalWeight",   ignore = true)
    @Mapping(target = "weightUom",     ignore = true)
    @Mapping(target = "totalVolume",   ignore = true)
    @Mapping(target = "volumeUom",     ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "createdDate",   ignore = true)
    @Mapping(target = "modifiedBy",    ignore = true)
    @Mapping(target = "modifiedDate",  ignore = true)
    @Mapping(target = "deleted",       ignore = true)
    @Mapping(target = "orgId",         ignore = true)
    @Mapping(target = "dcId",          ignore = true)
    @Mapping(target = "status",        ignore = true)
    @Mapping(target = "remarks",       ignore = true)
    void updateLineEntity(com.supplychain.outbound.outboundorder.application.dto.OutboundOrderLineUpdateRequestDto dto,
                          @MappingTarget OutboundOrderLine entity);
}
