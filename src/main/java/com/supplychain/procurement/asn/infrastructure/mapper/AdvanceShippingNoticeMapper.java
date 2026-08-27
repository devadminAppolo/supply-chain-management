package com.supplychain.procurement.asn.infrastructure.mapper;

import com.supplychain.procurement.asn.application.dto.AsnCreateRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnUpdateRequestDto;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AdvanceShippingNoticeLineMapper.class})
public interface AdvanceShippingNoticeMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "orgId",            ignore = true)
    @Mapping(target = "dcId",             ignore = true)
    @Mapping(target = "status",           ignore = true)
    @Mapping(target = "asnStatus",        ignore = true)
    @Mapping(target = "remarks",          ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "createdDate",      ignore = true)
    @Mapping(target = "modifiedBy",       ignore = true)
    @Mapping(target = "modifiedDate",     ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "purchaseOrder",    ignore = true)
    @Mapping(target = "lines",            ignore = true)
    AdvanceShippingNotice toEntity(AsnCreateRequestDto dto);

    @Mapping(target = "purchaseOrderId",     source = "purchaseOrder.id")
    @Mapping(target = "purchaseOrderNumber", source = "purchaseOrder.poNumber")
    AsnResponseDto toDto(AdvanceShippingNotice entity);

    List<AsnResponseDto> toDtoList(List<AdvanceShippingNotice> entities);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "orgId",            ignore = true)
    @Mapping(target = "dcId",             ignore = true)
    @Mapping(target = "status",           ignore = true)
    @Mapping(target = "remarks",          ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "createdDate",      ignore = true)
    @Mapping(target = "modifiedBy",       ignore = true)
    @Mapping(target = "modifiedDate",     ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "purchaseOrder",    ignore = true)
    @Mapping(target = "lines",            ignore = true)
    void updateEntity(AsnUpdateRequestDto dto, @MappingTarget AdvanceShippingNotice entity);
}
